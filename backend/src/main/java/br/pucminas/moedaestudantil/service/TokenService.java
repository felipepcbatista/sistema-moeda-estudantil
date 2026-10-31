package br.pucminas.moedaestudantil.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import br.pucminas.moedaestudantil.dto.SessaoResponse.Perfil;

/** Emite o JWT da sessao: subject = id do usuario, claims "perfil" e "nome", validade configuravel. */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final Duration validade;

    public TokenService(JwtEncoder encoder, @Value("${app.jwt.validade-horas}") long validadeHoras) {
        this.encoder = encoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    public String gerar(Long usuarioId, Perfil perfil, String nome) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("moeda-estudantil")
                .subject(usuarioId.toString())
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .claim("perfil", perfil.name())
                .claim("nome", nome)
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }
}
