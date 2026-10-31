package br.pucminas.moedaestudantil.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.Documentos;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.repository.AlunoRepository;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InstituicaoEnsinoRepository instituicaoRepository;
    private final TransacaoRepository transacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public AlunoService(AlunoRepository alunoRepository, UsuarioRepository usuarioRepository,
                        InstituicaoEnsinoRepository instituicaoRepository, TransacaoRepository transacaoRepository,
                        PasswordEncoder passwordEncoder) {
        this.alunoRepository = alunoRepository;
        this.usuarioRepository = usuarioRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.transacaoRepository = transacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** US02: todo aluno nasce com uma conta de saldo 0. */
    @Transactional
    public Aluno cadastrar(AlunoRequest dados) {
        if (dados.senha() == null || dados.senha().isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória no cadastro");
        }
        validarUnicidade(dados, null);
        Aluno aluno = new Aluno();
        aplicar(aluno, dados);
        aluno.setSenhaHash(passwordEncoder.encode(dados.senha()));
        return alunoRepository.save(aluno);
    }

    @Transactional(readOnly = true)
    public Aluno buscarPorId(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno", id));
    }

    /** US03: mesmas validacoes do cadastro; senha em branco mantem a atual, trocar exige a senha atual. */
    @Transactional
    public Aluno atualizar(Long id, AlunoRequest dados) {
        Aluno aluno = buscarPorId(id);
        validarUnicidade(dados, id);
        aplicar(aluno, dados);
        if (dados.senha() != null && !dados.senha().isBlank()) {
            if (!aluno.autenticar(dados.senhaAtual(), passwordEncoder)) {
                throw new RegraNegocioException("Senha atual incorreta");
            }
            aluno.setSenhaHash(passwordEncoder.encode(dados.senha()));
        }
        return aluno;
    }

    /** Aluno com moedas recebidas ou resgatadas nao e excluido, para preservar o extrato do professor. */
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = buscarPorId(id);
        if (transacaoRepository.existsMovimentacao(aluno.getConta().getId())) {
            throw new RegraNegocioException("Aluno possui movimentações de moedas e não pode ser excluído");
        }
        alunoRepository.delete(aluno);
    }

    private void validarUnicidade(AlunoRequest dados, Long id) {
        String cpf = Documentos.somenteDigitos(dados.cpf());
        String email = normalizarEmail(dados.email());
        String login = dados.login().trim();

        boolean cpfEmUso = id == null ? alunoRepository.existsByCpf(cpf) : alunoRepository.existsByCpfAndIdNot(cpf, id);
        if (cpfEmUso) {
            throw new RegraNegocioException("CPF já cadastrado");
        }
        boolean emailEmUso = id == null ? alunoRepository.existsByEmail(email) : alunoRepository.existsByEmailAndIdNot(email, id);
        if (emailEmUso) {
            throw new RegraNegocioException("Email já cadastrado");
        }
        boolean loginEmUso = id == null ? usuarioRepository.existsByLogin(login) : usuarioRepository.existsByLoginAndIdNot(login, id);
        if (loginEmUso) {
            throw new RegraNegocioException("Login já está em uso");
        }
    }

    private void aplicar(Aluno aluno, AlunoRequest dados) {
        InstituicaoEnsino instituicao = instituicaoRepository.findById(dados.instituicaoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Instituição", dados.instituicaoId()));
        aluno.setNome(dados.nome().trim());
        aluno.setEmail(normalizarEmail(dados.email()));
        aluno.setCpf(Documentos.somenteDigitos(dados.cpf()));
        aluno.setRg(dados.rg().trim());
        aluno.setCurso(dados.curso().trim());
        aluno.setEndereco(dados.endereco().toEntity());
        aluno.setInstituicao(instituicao);
        aluno.setLogin(dados.login().trim());
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
