package br.pucminas.moedaestudantil;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Simula um usuario logado nas requisicoes do MockMvc, com o mesmo formato do token real
 * (subject = id, claim "perfil", papel ROLE_<perfil>). O token real e testado em SegurancaTest.
 */
final class ComoUsuario {

    private ComoUsuario() {
    }

    static RequestPostProcessor aluno(long id) {
        return como("ALUNO", id);
    }

    static RequestPostProcessor professor(long id) {
        return como("PROFESSOR", id);
    }

    static RequestPostProcessor empresa(long id) {
        return como("EMPRESA", id);
    }

    private static RequestPostProcessor como(String perfil, long id) {
        return jwt()
                .jwt(j -> j.subject(String.valueOf(id)).claim("perfil", perfil))
                .authorities(new SimpleGrantedAuthority("ROLE_" + perfil));
    }
}
