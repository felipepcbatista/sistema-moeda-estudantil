package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.Professor;

public record ProfessorResponse(Long id, String nome, String departamento, InstituicaoResponse instituicao, int saldo) {

    public static ProfessorResponse from(Professor p) {
        return new ProfessorResponse(p.getId(), p.getNome(), p.getDepartamento(),
                InstituicaoResponse.from(p.getInstituicao()), p.getConta().getSaldo());
    }
}
