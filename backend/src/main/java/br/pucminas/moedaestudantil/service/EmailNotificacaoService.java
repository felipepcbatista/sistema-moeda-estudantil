package br.pucminas.moedaestudantil.service;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.ProfessorRepository;

/**
 * Monta os emails do sistema e os entrega pelo {@link CanalEmail} configurado (SMTP local ou API HTTP na nuvem).
 * A mensagem e montada dentro da transacao, mas so e enviada depois do commit: um email nunca avisa
 * de uma transacao que acabou desfeita, e uma falha no envio nunca desfaz a transacao (so e registrada no log).
 */
@Service
public class EmailNotificacaoService implements NotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificacaoService.class);
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final CanalEmail canal;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;

    public EmailNotificacaoService(CanalEmail canal, AlunoRepository alunoRepository,
                                   ProfessorRepository professorRepository) {
        this.canal = canal;
        this.alunoRepository = alunoRepository;
        this.professorRepository = professorRepository;
    }

    @Override
    public void notificarRecebimento(EnvioMoedas envio) {
        Aluno aluno = alunoRepository.findByContaId(envio.getContaDestino().getId()).orElseThrow();
        Professor professor = professorRepository.findByContaId(envio.getContaOrigem().getId()).orElseThrow();
        enviarAposCommit(aluno.getEmail(),
                "Você recebeu " + envio.getValor() + " moedas!",
                """
                Olá, %s!

                O(a) professor(a) %s reconheceu o seu mérito e enviou %d moedas para você.

                Motivo: %s

                Seu saldo agora é de %d moedas. Troque suas moedas por vantagens no Sistema de Moeda Estudantil.
                """.formatted(aluno.getNome(), professor.getNome(), envio.getValor(), envio.getMotivo(),
                        envio.getContaDestino().getSaldo()));
    }

    @Override
    public void enviarCupomAluno(ResgateVantagem resgate) {
        Aluno aluno = alunoRepository.findByContaId(resgate.getContaOrigem().getId()).orElseThrow();
        EmpresaParceira empresa = resgate.getVantagem().getEmpresa();
        enviarAposCommit(aluno.getEmail(),
                "Seu cupom: " + resgate.getVantagem().getNome() + " (código " + resgate.getCodigo() + ")",
                """
                Olá, %s!

                Seu resgate foi confirmado.

                Vantagem: %s
                Empresa: %s
                Custo: %d moedas
                Data: %s

                CÓDIGO DO CUPOM: %s

                Apresente este código na empresa parceira para realizar a troca.
                """.formatted(aluno.getNome(), resgate.getVantagem().getNome(), empresa.getNomeFantasia(),
                        resgate.getValor(), resgate.getDataHora().format(DATA), resgate.getCodigo()));
    }

    @Override
    public void notificarParceiro(ResgateVantagem resgate) {
        Aluno aluno = alunoRepository.findByContaId(resgate.getContaOrigem().getId()).orElseThrow();
        EmpresaParceira empresa = resgate.getVantagem().getEmpresa();
        enviarAposCommit(empresa.getEmail(),
                "Novo resgate: " + resgate.getVantagem().getNome() + " (código " + resgate.getCodigo() + ")",
                """
                Olá, %s!

                Um aluno resgatou uma vantagem oferecida por vocês.

                Vantagem: %s
                Aluno: %s
                Data: %s

                CÓDIGO PARA CONFERÊNCIA: %s

                Na troca presencial, confira se o código apresentado pelo aluno é o mesmo deste email.
                """.formatted(empresa.getNomeFantasia(), resgate.getVantagem().getNome(), aluno.getNome(),
                        resgate.getDataHora().format(DATA), resgate.getCodigo()));
    }

    private void enviarAposCommit(String para, String assunto, String texto) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    enviar(para, assunto, texto);
                }
            });
        } else {
            enviar(para, assunto, texto);
        }
    }

    private void enviar(String para, String assunto, String texto) {
        try {
            canal.enviar(para, assunto, texto);
        } catch (RuntimeException e) {
            log.warn("Falha ao enviar email para {}: {}", para, e.getMessage());
        }
    }
}
