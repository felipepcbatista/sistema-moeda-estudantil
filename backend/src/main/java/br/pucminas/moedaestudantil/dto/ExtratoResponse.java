package br.pucminas.moedaestudantil.dto;

import java.util.List;

/** US08: saldo atual + transacoes em ordem cronologica decrescente. */
public record ExtratoResponse(int saldo, List<TransacaoResponse> transacoes) {
}
