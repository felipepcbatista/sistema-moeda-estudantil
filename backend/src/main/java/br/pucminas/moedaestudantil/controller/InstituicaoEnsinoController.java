package br.pucminas.moedaestudantil.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.moedaestudantil.dto.InstituicaoResponse;
import br.pucminas.moedaestudantil.service.InstituicaoEnsinoService;

@RestController
@RequestMapping("/api/instituicoes")
public class InstituicaoEnsinoController {

    private final InstituicaoEnsinoService service;

    public InstituicaoEnsinoController(InstituicaoEnsinoService service) {
        this.service = service;
    }

    @GetMapping
    public List<InstituicaoResponse> listar() {
        return service.listar().stream().map(InstituicaoResponse::from).toList();
    }
}
