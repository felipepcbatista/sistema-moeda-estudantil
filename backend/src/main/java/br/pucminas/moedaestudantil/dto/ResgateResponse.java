package br.pucminas.moedaestudantil.dto;

import java.time.LocalDateTime;

import br.pucminas.moedaestudantil.model.ResgateVantagem;

/** Comprovante do resgate: o codigo e o mesmo enviado por email ao aluno e a empresa (RN10). */
public record ResgateResponse(
        Long id,
        String codigo,
        String vantagem,
        String empresa,
        int valor,
        LocalDateTime dataHora,
        int saldoRestante) {

    public static ResgateResponse from(ResgateVantagem r) {
        return new ResgateResponse(r.getId(), r.getCodigo(), r.getVantagem().getNome(),
                r.getVantagem().getEmpresa().getNomeFantasia(), r.getValor(), r.getDataHora(),
                r.getContaOrigem().getSaldo());
    }
}
