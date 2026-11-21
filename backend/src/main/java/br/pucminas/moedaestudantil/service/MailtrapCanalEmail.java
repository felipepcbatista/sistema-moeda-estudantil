package br.pucminas.moedaestudantil.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Envio pela API HTTP do Mailtrap (porta 443). Usado na nuvem, onde o Render gratuito bloqueia SMTP.
 * Com a URL do Sandbox (https://sandbox.api.mailtrap.io/api/send/{inbox}), os emails caem numa caixa de
 * testes, como no Mailpit: os enderecos dos dados de demonstracao sao ficticios e nao recebem nada de verdade.
 */
@Component
@ConditionalOnProperty(name = "app.mail.provedor", havingValue = "mailtrap")
public class MailtrapCanalEmail implements CanalEmail {

    private final RestClient http;
    private final String remetente;

    public MailtrapCanalEmail(RestClient.Builder builder,
                              @Value("${app.mail.mailtrap.url}") String url,
                              @Value("${app.mail.mailtrap.token}") String token,
                              @Value("${app.mail.remetente}") String remetente) {
        this.http = builder.baseUrl(url).defaultHeader("Authorization", "Bearer " + token).build();
        this.remetente = remetente;
    }

    @Override
    public void enviar(String para, String assunto, String texto) {
        http.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", Map.of("email", remetente, "name", "Moeda Estudantil"),
                        "to", List.of(Map.of("email", para)),
                        "subject", assunto,
                        "text", texto,
                        "category", "moeda-estudantil"))
                .retrieve()
                .toBodilessEntity();
    }
}
