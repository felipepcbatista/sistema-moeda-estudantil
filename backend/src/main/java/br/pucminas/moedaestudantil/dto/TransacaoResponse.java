package br.pucminas.moedaestudantil.dto;

import java.time.LocalDateTime;

/**
 * Linha do extrato, ja do ponto de vista de quem consulta: valor negativo = saiu da conta,
 * positivo = entrou. "descricao" diz com quem foi a transacao.
 */
public record TransacaoResponse(
        Long id,
        String tipo,
        int valor,
        LocalDateTime dataHora,
        String descricao,
        String motivo,
        String codigo) {
}
