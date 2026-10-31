package br.pucminas.moedaestudantil.dto;

import br.pucminas.moedaestudantil.model.EmpresaParceira;

public record EmpresaParceiraResponse(Long id, String nomeFantasia, String cnpj, String email, String login) {

    public static EmpresaParceiraResponse from(EmpresaParceira e) {
        return new EmpresaParceiraResponse(e.getId(), e.getNomeFantasia(), e.getCnpj(), e.getEmail(), e.getLogin());
    }
}
