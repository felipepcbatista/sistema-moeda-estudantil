package br.pucminas.moedaestudantil.dto;

import java.time.LocalDateTime;

/** Linha da tela de conferencia da empresa (RN10): o codigo que o aluno vai apresentar na troca. */
public record ResgateEmpresaResponse(
        Long id,
        String codigo,
        String vantagem,
        String aluno,
        int valor,
        LocalDateTime dataHora) {
}
