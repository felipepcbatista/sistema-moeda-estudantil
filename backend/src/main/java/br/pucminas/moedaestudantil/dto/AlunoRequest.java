package br.pucminas.moedaestudantil.dto;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados de cadastro/edicao de aluno (RN01). A senha e obrigatoria no cadastro e opcional na edicao
 * (em branco = manter a atual); essa diferenca e checada no AlunoService.
 */
public record AlunoRequest(
        @NotBlank(message = "Nome é obrigatório") @Size(max = 150) String nome,
        @NotBlank(message = "Email é obrigatório") @Email(message = "Email inválido") @Size(max = 150) String email,
        @NotBlank(message = "CPF é obrigatório") @CPF(message = "CPF inválido") String cpf,
        @NotBlank(message = "RG é obrigatório") @Size(max = 20) String rg,
        @NotBlank(message = "Curso é obrigatório") @Size(max = 100) String curso,
        @NotNull(message = "Endereço é obrigatório") @Valid EnderecoDTO endereco,
        @NotNull(message = "Instituição é obrigatória") Long instituicaoId,
        @NotBlank(message = "Login é obrigatório") @Size(min = 4, max = 60, message = "Login deve ter entre 4 e 60 caracteres") String login,
        @Size(min = 6, max = 72, message = "Senha deve ter entre 6 e 72 caracteres") String senha) {
}
