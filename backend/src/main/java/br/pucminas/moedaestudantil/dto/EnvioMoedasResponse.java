package br.pucminas.moedaestudantil.dto;

import java.time.LocalDateTime;

import br.pucminas.moedaestudantil.model.EnvioMoedas;

public record EnvioMoedasResponse(Long id, int valor, String motivo, LocalDateTime dataHora, int saldoRestante) {

    public static EnvioMoedasResponse from(EnvioMoedas e) {
        return new EnvioMoedasResponse(e.getId(), e.getValor(), e.getMotivo(), e.getDataHora(),
                e.getContaOrigem().getSaldo());
    }
}
