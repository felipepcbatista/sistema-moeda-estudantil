package br.pucminas.moedaestudantil.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Componente "Agendador de Credito Semestral" (RN04): processo de sistema disparado por data, sem ator.
 * Roda a meia-noite de 1o de fevereiro e de 1o de agosto, e tambem ao subir a aplicacao, para cobrir o caso
 * de o servidor estar desligado na virada. Como o credito e idempotente, rodar de novo nao duplica moedas.
 */
@Component
public class CreditoSemestralJob {

    private static final Logger log = LoggerFactory.getLogger(CreditoSemestralJob.class);

    private final TransacaoService transacaoService;

    public CreditoSemestralJob(TransacaoService transacaoService) {
        this.transacaoService = transacaoService;
    }

    @Scheduled(cron = "0 0 0 1 2,8 *")
    @EventListener(ApplicationReadyEvent.class)
    public void executar() {
        String semestre = semestreDe(LocalDate.now());
        int creditados = transacaoService.creditarSemestre(semestre);
        log.info("Crédito semestral {}: {} professor(es) creditado(s)", semestre, creditados);
    }

    /** Fevereiro a julho = semestre 1; agosto a janeiro = semestre 2 (janeiro ainda pertence ao ano anterior). */
    public static String semestreDe(LocalDate data) {
        int mes = data.getMonthValue();
        if (mes == 1) {
            return (data.getYear() - 1) + "-2";
        }
        return data.getYear() + (mes < 8 ? "-1" : "-2");
    }
}
