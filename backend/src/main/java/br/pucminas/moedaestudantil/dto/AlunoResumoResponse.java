package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Aluno;

/** O que o professor precisa para reconhecer um aluno, sem dados pessoais (CPF, RG, endereco, email). */
public record AlunoResumoResponse(Long id, String nome, String curso, int saldo) {

    public static AlunoResumoResponse from(Aluno a) {
        return new AlunoResumoResponse(a.getId(), a.getNome(), a.getCurso(), a.getConta().getSaldo());
    }
}
