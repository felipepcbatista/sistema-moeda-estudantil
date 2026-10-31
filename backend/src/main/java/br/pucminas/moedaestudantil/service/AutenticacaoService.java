package br.pucminas.moedaestudantil.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.dto.SessaoResponse;
import br.pucminas.moedaestudantil.dto.SessaoResponse.Perfil;
import br.pucminas.moedaestudantil.exception.CredenciaisInvalidasException;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.model.Professor;
import br.pucminas.moedaestudantil.model.Usuario;
import br.pucminas.moedaestudantil.repository.UsuarioRepository;

/**
 * US01: confere login e senha e identifica o perfil.
 * Nesta versao nao emite token; a protecao das rotas com Spring Security + JWT e da Sprint 3.
 */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public SessaoResponse login(String login, String senha) {
        Usuario usuario = usuarioRepository.findByLogin(login.trim())
                .filter(u -> u.autenticar(senha, passwordEncoder))
                // mesma mensagem para login inexistente e senha errada: nao revela quais logins existem
                .orElseThrow(CredenciaisInvalidasException::new);

        return switch (usuario) {
            case Aluno a -> new SessaoResponse(a.getId(), Perfil.ALUNO, a.getNome());
            case Professor p -> new SessaoResponse(p.getId(), Perfil.PROFESSOR, p.getNome());
            case EmpresaParceira e -> new SessaoResponse(e.getId(), Perfil.EMPRESA, e.getNomeFantasia());
            default -> throw new IllegalStateException("Perfil desconhecido: " + usuario.getClass());
        };
    }
}
