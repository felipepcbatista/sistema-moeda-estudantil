package br.pucminas.moedaestudantil.dto;

import org.hibernate.validator.constraints.br.CNPJ;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Mesma regra de senha do AlunoRequest: obrigatoria no cadastro, opcional na edicao. */
public record EmpresaParceiraRequest(
        @NotBlank(message = "Nome fantasia é obrigatório") @Size(max = 150) String nomeFantasia,
        @NotBlank(message = "CNPJ é obrigatório") @CNPJ(message = "CNPJ inválido") String cnpj,
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") @Size(max = 150) String email,
        @NotBlank(message = "Login é obrigatório") @Size(min = 4, max = 60, message = "Login deve ter entre 4 e 60 caracteres") String login,
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres") String senha) {
}
