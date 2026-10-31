package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** US07: aluno, quantidade e motivo (mensagem aberta e obrigatoria, RN05). */
public record EnvioMoedasRequest(
        @NotNull(message = "Selecione o aluno") Long alunoId,
        @NotNull(message = "Informe a quantidade") @Positive(message = "A quantidade deve ser maior que zero") Integer valor,
        @NotBlank(message = "O motivo é obrigatório") @Size(max = 500, message = "Motivo com no máximo 500 caracteres") String motivo) {
}
