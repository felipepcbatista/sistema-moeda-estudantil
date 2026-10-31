package br.pucminas.moedaestudantil.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** RN08: nome, descricao e custo; a foto chega como arquivo separado na mesma requisicao multipart. */
public record VantagemRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 120) String nome,
        @NotBlank(message = "Descrição é obrigatória") @Size(max = 500, message = "Descrição com no máximo 500 caracteres") String descricao,
        @NotNull(message = "Informe o custo") @Positive(message = "O custo deve ser maior que zero") Integer custoMoedas) {
}
