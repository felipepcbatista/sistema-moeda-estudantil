package br.pucminas.moedaestudantil.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Envio por SMTP (JavaMailSender). Padrao em desenvolvimento: o Mailpit do docker-compose recebe tudo. */
@Component
@ConditionalOnProperty(name = "app.mail.provedor", havingValue = "smtp", matchIfMissing = true)
public class SmtpCanalEmail implements CanalEmail {

    private final JavaMailSender mailSender;
    private final String remetente;

    public SmtpCanalEmail(JavaMailSender mailSender, @Value("${app.mail.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Override
    public void enviar(String para, String assunto, String texto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        mailSender.send(mensagem);
    }
}
