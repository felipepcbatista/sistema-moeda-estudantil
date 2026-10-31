package br.pucminas.moedaestudantil.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.dto.Documentos;
import br.pucminas.moedaestudantil.dto.EmpresaParceiraRequest;
import br.pucminas.moedaestudantil.exception.RecursoNaoEncontradoException;
import br.pucminas.moedaestudantil.exception.RegraNegocioException;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.repository.EmpresaParceiraRepository;
import br.pucminas.moedaestudantil.repository.TransacaoRepository;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;

@Service
public class EmpresaParceiraService {

    private final EmpresaParceiraRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TransacaoRepository transacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public EmpresaParceiraService(EmpresaParceiraRepository empresaRepository, UsuarioRepository usuarioRepository,
                                  TransacaoRepository transacaoRepository, PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.usuarioRepository = usuarioRepository;
        this.transacaoRepository = transacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** US04 */
    @Transactional
    public EmpresaParceira cadastrar(EmpresaParceiraRequest dados) {
        if (dados.senha() == null || dados.senha().isBlank()) {
            throw new RegraNegocioException("Senha é obrigatória no cadastro");
        }
        validarUnicidade(dados, null);
        EmpresaParceira empresa = new EmpresaParceira();
        aplicar(empresa, dados);
        empresa.setSenhaHash(passwordEncoder.encode(dados.senha()));
        return empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public EmpresaParceira buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empresa parceira", id));
    }

    @Transactional(readOnly = true)
    public List<EmpresaParceira> listar() {
        return empresaRepository.findAllByOrderByNomeFantasiaAsc();
    }

    /** US05: senha em branco mantem a atual. */
    @Transactional
    public EmpresaParceira atualizar(Long id, EmpresaParceiraRequest dados) {
        EmpresaParceira empresa = buscarPorId(id);
        validarUnicidade(dados, id);
        aplicar(empresa, dados);
        if (dados.senha() != null && !dados.senha().isBlank()) {
            empresa.setSenhaHash(passwordEncoder.encode(dados.senha()));
        }
        return empresa;
    }

    /** Vantagens ja resgatadas sustentam cupons emitidos; a empresa com resgates nao e excluida. */
    @Transactional
    public void excluir(Long id) {
        EmpresaParceira empresa = buscarPorId(id);
        if (transacaoRepository.existsResgateDaEmpresa(id)) {
            throw new RegraNegocioException("Empresa possui vantagens já resgatadas e não pode ser excluída");
        }
        empresaRepository.delete(empresa);
    }

    private void validarUnicidade(EmpresaParceiraRequest dados, Long id) {
        String cnpj = Documentos.somenteDigitos(dados.cnpj());
        String email = dados.email().trim().toLowerCase();
        String login = dados.login().trim();

        boolean cnpjEmUso = id == null ? empresaRepository.existsByCnpj(cnpj) : empresaRepository.existsByCnpjAndIdNot(cnpj, id);
        if (cnpjEmUso) {
            throw new RegraNegocioException("CNPJ já cadastrado");
        }
        boolean emailEmUso = id == null ? empresaRepository.existsByEmail(email) : empresaRepository.existsByEmailAndIdNot(email, id);
        if (emailEmUso) {
            throw new RegraNegocioException("Email já cadastrado");
        }
        boolean loginEmUso = id == null ? usuarioRepository.existsByLogin(login) : usuarioRepository.existsByLoginAndIdNot(login, id);
        if (loginEmUso) {
            throw new RegraNegocioException("Login já está em uso");
        }
    }

    private static void aplicar(EmpresaParceira empresa, EmpresaParceiraRequest dados) {
        empresa.setNomeFantasia(dados.nomeFantasia().trim());
        empresa.setCnpj(Documentos.somenteDigitos(dados.cnpj()));
        empresa.setEmail(dados.email().trim().toLowerCase());
        empresa.setLogin(dados.login().trim());
    }
}
