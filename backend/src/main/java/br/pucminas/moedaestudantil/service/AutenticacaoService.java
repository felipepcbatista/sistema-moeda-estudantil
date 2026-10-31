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

/** US01: confere login e senha, identifica o perfil e emite o token JWT da sessao. */
@Service
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AutenticacaoService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                               TokenService tokenService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public SessaoResponse login(String login, String senha) {
        Usuario usuario = usuarioRepository.findByLogin(login.trim())
                .filter(u -> u.autenticar(senha, passwordEncoder))
                // mesma mensagem para login inexistente e senha errada: nao revela quais logins existem
                .orElseThrow(CredenciaisInvalidasException::new);

        Perfil perfil;
        String nome;
        switch (usuario) {
            case Aluno a -> { perfil = Perfil.ALUNO; nome = a.getNome(); }
            case Professor p -> { perfil = Perfil.PROFESSOR; nome = p.getNome(); }
            case EmpresaParceira e -> { perfil = Perfil.EMPRESA; nome = e.getNomeFantasia(); }
            default -> throw new IllegalStateException("Perfil desconhecido: " + usuario.getClass());
        }
        return new SessaoResponse(usuario.getId(), perfil, nome, tokenService.gerar(usuario.getId(), perfil, nome));
    }
}
