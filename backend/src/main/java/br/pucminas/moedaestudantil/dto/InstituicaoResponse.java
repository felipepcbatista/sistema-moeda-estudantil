package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;

public record InstituicaoResponse(Long id, String nome) {

    public static InstituicaoResponse from(InstituicaoEnsino i) {
        return new InstituicaoResponse(i.getId(), i.getNome());
    }
}
