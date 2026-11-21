package br.pucminas.moedaestudantil;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import br.pucminas.moedaestudantil.service.MailtrapCanalEmail;

/** Canal de email da nuvem: monta a chamada HTTP que o Mailtrap espera, sem acessar a internet. */
class MailtrapCanalEmailTest {

    private static final String URL = "https://sandbox.api.mailtrap.io/api/send/123";

    @Test
    void enviaPelaApiComTokenERemetente() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        MailtrapCanalEmail canal = new MailtrapCanalEmail(builder, URL, "tok-123", "nao-responda@moeda.local");

        servidor.expect(requestTo(URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer tok-123"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.from.email").value("nao-responda@moeda.local"))
                .andExpect(jsonPath("$.to[0].email").value("maria@email.com"))
                .andExpect(jsonPath("$.subject").value("Você recebeu 50 moedas!"))
                .andExpect(jsonPath("$.text").value("corpo"))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));

        canal.enviar("maria@email.com", "Você recebeu 50 moedas!", "corpo");
        servidor.verify();
    }

    @Test
    void erroDaApiViraExcecaoParaONotificadorRegistrarNoLog() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer servidor = MockRestServiceServer.bindTo(builder).build();
        MailtrapCanalEmail canal = new MailtrapCanalEmail(builder, URL, "token-errado", "x@moeda.local");
        servidor.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> canal.enviar("maria@email.com", "assunto", "corpo"))
                .isInstanceOf(RestClientException.class);
    }
}
