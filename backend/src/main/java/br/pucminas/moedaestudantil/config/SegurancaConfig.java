package br.pucminas.moedaestudantil.config;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import br.pucminas.moedaestudantil.exception.ErroResponse;
import jakarta.servlet.http.HttpServletResponse;

/**
 * US01: toda operacao exige autenticacao, exceto o que um visitante precisa (login, cadastro de aluno e de
 * empresa, instituicoes, vitrine de vantagens e fotos). O token JWT carrega o id do usuario (subject) e o perfil;
 * as regras "so o proprio dono" ficam nos controllers, com @PreAuthorize + o componente Acesso.
 */
@Configuration
@EnableMethodSecurity
public class SegurancaConfig {

    @Bean
    public SecurityFilterChain filtro(HttpSecurity http, AuthenticationEntryPoint naoAutenticado,
                                      AccessDeniedHandler semPermissao) throws Exception {
        return http
                // API stateless com token no cabecalho Authorization (nao usa cookie de sessao): CSRF nao se aplica
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/alunos", "/api/empresas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/instituicoes", "/api/vantagens", "/api/vantagens/*",
                                "/api/fotos/*").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(o -> o
                        .jwt(j -> j.jwtAuthenticationConverter(perfilComoPapel()))
                        .authenticationEntryPoint(naoAutenticado))
                .exceptionHandling(e -> e.authenticationEntryPoint(naoAutenticado).accessDeniedHandler(semPermissao))
                .build();
    }

    /** O claim "perfil" (ALUNO, PROFESSOR, EMPRESA) vira o papel ROLE_ALUNO etc., usado em hasRole(...). */
    private static JwtAuthenticationConverter perfilComoPapel() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(jwt ->
                List.of(new SimpleGrantedAuthority("ROLE_" + jwt.getClaimAsString("perfil"))));
        return conversor;
    }

    @Bean
    public SecretKey chaveJwt(@Value("${app.jwt.segredo}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.segredo precisa ter pelo menos 32 bytes (HS256)");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey chaveJwt) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chaveJwt));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey chaveJwt) {
        return NimbusJwtDecoder.withSecretKey(chaveJwt).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** 401 no mesmo formato de erro do resto da API. */
    @Bean
    public AuthenticationEntryPoint naoAutenticado(ObjectMapper json) {
        return (req, resp, e) -> escrever(resp, json, HttpStatus.UNAUTHORIZED,
                "Faça login para continuar (sessão ausente ou expirada)");
    }

    /** 403: autenticado, mas tentando acessar dados de outro usuario ou funcao de outro perfil. */
    @Bean
    public AccessDeniedHandler semPermissao(ObjectMapper json) {
        return (req, resp, e) -> escrever(resp, json, HttpStatus.FORBIDDEN,
                "Você não tem permissão para acessar este recurso");
    }

    private static void escrever(HttpServletResponse resp, ObjectMapper json, HttpStatus status, String mensagem)
            throws java.io.IOException {
        resp.setStatus(status.value());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        json.writeValue(resp.getOutputStream(), new ErroResponse(status.value(), mensagem));
    }
}
