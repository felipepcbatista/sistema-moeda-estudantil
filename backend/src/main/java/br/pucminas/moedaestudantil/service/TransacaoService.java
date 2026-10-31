package br.pucminas.moedaestudantil.service;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ResgateEmpresaResponse;
import br.pucminas.moedaestudantil.dto.TransacaoResponse;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.Conta;
import br.pucminas.moedaestudantil.model.CreditoSemestral;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Transacao;
import br.pucminas.moedaestudantil.model.Vantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.VantagemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

/**
 * Toda movimentacao de moedas passa por aqui. Cada operacao e atomica: debito, credito e registro da
 * transacao acontecem juntos ou nao acontecem. As contas envolvidas sao travadas (SELECT ... FOR UPDATE)
 * antes de conferir o saldo, para que duas operacoes simultaneas nao leiam o mesmo saldo e uma sobrescreva a outra.
 */
@Service
public class TransacaoService {

    public static final int CREDITO_POR_SEMESTRE = 1000;

    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sem 0/O, 1/I
    private static final int TAMANHO_CODIGO = 8;

    private final TransacaoRepository transacaoRepository;
    private final ProfessorRepository professorRepository;
    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final NotificacaoService notificacaoService;
    private final EntityManager em;
    private final SecureRandom random = new SecureRandom();

    public TransacaoService(TransacaoRepository transacaoRepository, ProfessorRepository professorRepository,
                            AlunoRepository alunoRepository, VantagemRepository vantagemRepository,
                            NotificacaoService notificacaoService, EntityManager em) {
        this.transacaoRepository = transacaoRepository;
        this.professorRepository = professorRepository;
        this.alunoRepository = alunoRepository;
        this.vantagemRepository = vantagemRepository;
        this.notificacaoService = notificacaoService;
        this.em = em;
    }

    /** US07 / RN05 / RN06 */
    @Transactional
    public EnvioMoedas enviarMoedas(Long professorId, Long alunoId, int valor, String motivo) {
        if (valor <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new RegraNegocioException("O motivo do reconhecimento é obrigatório");
        }
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", professorId));
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", alunoId));
        if (!professor.getInstituicao().getId().equals(aluno.getInstituicao().getId())) {
            throw new RegraNegocioException("O aluno não pertence à instituição do professor");
        }

        travar(professor.getConta(), aluno.getConta());
        if (!professor.getConta().possuiSaldo(valor)) {
            throw new RegraNegocioException("Saldo insuficiente: você tem " + professor.getConta().getSaldo() + " moedas");
        }
        professor.getConta().debitar(valor);
        aluno.getConta().creditar(valor);

        EnvioMoedas envio = new EnvioMoedas();
        envio.setValor(valor);
        envio.setMotivo(motivo.trim());
        envio.setContaOrigem(professor.getConta());
        envio.setContaDestino(aluno.getConta());
        transacaoRepository.save(envio);

        notificacaoService.notificarRecebimento(envio);
        return envio;
    }

    /** US10 / RN09 / RN10 */
    @Transactional
    public ResgateVantagem resgatarVantagem(Long alunoId, Long vantagemId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", alunoId));
        Vantagem vantagem = vantagemRepository.findById(vantagemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vantagem", vantagemId));
        if (!vantagem.isAtiva()) {
            throw new RegraNegocioException("Esta vantagem não está mais disponível");
        }

        travar(aluno.getConta());
        int custo = vantagem.getCustoMoedas();
        if (!aluno.getConta().possuiSaldo(custo)) {
            throw new RegraNegocioException("Saldo insuficiente: a vantagem custa " + custo
                    + " moedas e você tem " + aluno.getConta().getSaldo());
        }
        aluno.getConta().debitar(custo);

        ResgateVantagem resgate = new ResgateVantagem();
        resgate.setValor(custo);
        resgate.setContaOrigem(aluno.getConta());
        resgate.setVantagem(vantagem);
        resgate.setCodigo(gerarCodigo());
        transacaoRepository.save(resgate);

        notificacaoService.enviarCupomAluno(resgate);
        notificacaoService.notificarParceiro(resgate);
        return resgate;
    }

    /**
     * RN04: soma 1.000 moedas ao saldo corrente de cada professor (acumulavel).
     * Idempotente por semestre: professor ja creditado naquele semestre e ignorado.
     * @return quantos professores foram creditados
     */
    @Transactional
    public int creditarSemestre(String semestre) {
        int creditados = 0;
        for (Professor professor : professorRepository.findAll()) {
            Conta conta = professor.getConta();
            if (transacaoRepository.existsCreditoSemestre(conta.getId(), semestre)) {
                continue;
            }
            travar(conta);
            conta.creditar(CREDITO_POR_SEMESTRE);
            CreditoSemestral credito = new CreditoSemestral();
            credito.setValor(CREDITO_POR_SEMESTRE);
            credito.setSemestre(semestre);
            credito.setContaDestino(conta);
            transacaoRepository.save(credito);
            creditados++;
        }
        return creditados;
    }

    @Transactional(readOnly = true)
    public List<Transacao> consultarExtrato(Long contaId) {
        return transacaoRepository.findExtrato(contaId);
    }

    /** US08 (professor): creditos semestrais e envios. */
    @Transactional(readOnly = true)
    public ExtratoResponse extratoProfessor(Long professorId) {
        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor", professorId));
        return montarExtrato(professor.getConta());
    }

    /** US08 (aluno): recebimentos e resgates. */
    @Transactional(readOnly = true)
    public ExtratoResponse extratoAluno(Long alunoId) {
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", alunoId));
        return montarExtrato(aluno.getConta());
    }

    /** RN10: resgates das vantagens da empresa, com o codigo que ela confere na troca presencial. */
    @Transactional(readOnly = true)
    public List<ResgateEmpresaResponse> resgatesDaEmpresa(Long empresaId) {
        Map<Long, String> nomesPorConta = new HashMap<>();
        return transacaoRepository.findResgatesDaEmpresa(empresaId).stream()
                .map(r -> new ResgateEmpresaResponse(r.getId(), r.getCodigo(), r.getVantagem().getNome(),
                        nomeDoDono(r.getContaOrigem().getId(), nomesPorConta), r.getValor(), r.getDataHora()))
                .toList();
    }

    private ExtratoResponse montarExtrato(Conta conta) {
        Map<Long, String> nomesPorConta = new HashMap<>();
        List<TransacaoResponse> linhas = consultarExtrato(conta.getId()).stream()
                .map(t -> paraLinha(t, conta.getId(), nomesPorConta))
                .toList();
        return new ExtratoResponse(conta.getSaldo(), linhas);
    }

    private TransacaoResponse paraLinha(Transacao t, Long contaId, Map<Long, String> nomesPorConta) {
        boolean saida = t.getContaOrigem() != null && t.getContaOrigem().getId().equals(contaId);
        int valor = saida ? -t.getValor() : t.getValor();

        return switch (t) {
            case EnvioMoedas e -> new TransacaoResponse(e.getId(), "ENVIO", valor, e.getDataHora(),
                    saida ? "Enviado para " + nomeDoDono(e.getContaDestino().getId(), nomesPorConta)
                          : "Recebido de " + nomeDoDono(e.getContaOrigem().getId(), nomesPorConta),
                    e.getMotivo(), null);
            case ResgateVantagem r -> new TransacaoResponse(r.getId(), "RESGATE", valor, r.getDataHora(),
                    "Resgate: " + r.getVantagem().getNome() + " (" + r.getVantagem().getEmpresa().getNomeFantasia() + ")",
                    null, r.getCodigo());
            case CreditoSemestral c -> new TransacaoResponse(c.getId(), "CREDITO", valor, c.getDataHora(),
                    "Crédito semestral " + c.getSemestre(), null, null);
            default -> throw new IllegalStateException("Tipo de transação desconhecido: " + t.getClass());
        };
    }

    /** Nome do aluno ou professor dono da conta, com cache para nao repetir a consulta no mesmo extrato. */
    private String nomeDoDono(Long contaId, Map<Long, String> cache) {
        return cache.computeIfAbsent(contaId, id -> alunoRepository.findByContaId(id).map(Aluno::getNome)
                .or(() -> professorRepository.findByContaId(id).map(Professor::getNome))
                .orElse("conta " + id));
    }

    /**
     * Trava as contas em ordem de id (evita deadlock entre operacoes cruzadas) e recarrega o saldo do banco.
     * O flush antes grava alteracoes pendentes, que o refresh descartaria.
     */
    private void travar(Conta... contas) {
        em.flush();
        Arrays.stream(contas)
                .sorted(Comparator.comparing(Conta::getId))
                .forEach(c -> em.refresh(c, LockModeType.PESSIMISTIC_WRITE));
    }

    private String gerarCodigo() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder(TAMANHO_CODIGO);
            for (int i = 0; i < TAMANHO_CODIGO; i++) {
                sb.append(ALFABETO_CODIGO.charAt(random.nextInt(ALFABETO_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (transacaoRepository.existsCodigoResgate(codigo));
        return codigo;
    }
}
