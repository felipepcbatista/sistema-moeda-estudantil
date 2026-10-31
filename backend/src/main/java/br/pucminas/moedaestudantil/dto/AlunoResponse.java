package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

/** Nunca expoe a senha; inclui o saldo da conta para a listagem. */
public record AlunoResponse(
        Long id,
        String nome,
        String email,
        String cpf,
        String rg,
        String curso,
        EnderecoDTO endereco,
        InstituicaoResponse instituicao,
        String login,
        int saldo) {

    public static AlunoResponse from(Aluno a) {
        return new AlunoResponse(a.getId(), a.getNome(), a.getEmail(), a.getCpf(), a.getRg(), a.getCurso(),
                EnderecoDTO.from(a.getEndereco()), InstituicaoResponse.from(a.getInstituicao()),
                a.getLogin(), a.getConta().getSaldo());
    }
}
