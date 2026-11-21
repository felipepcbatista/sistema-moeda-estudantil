package br.pucminas.moedaestudantil.service;

/**
 * Como o email sai do sistema. Escolhido pela propriedade app.mail.provedor:
 * "smtp" (padrao, desenvolvimento com o Mailpit) ou "mailtrap" (nuvem, API HTTP).
 * Existe porque o Render gratuito bloqueia as portas SMTP de saida (25, 465 e 587).
 */
public interface CanalEmail {

    void enviar(String para, String assunto, String texto);
}
