package br.pucminas.moedaestudantil.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.moedaestudantil.dto.LoginRequest;
import br.pucminas.moedaestudantil.dto.SessaoResponse;
import br.pucminas.moedaestudantil.service.AutenticacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService service;

    public AutenticacaoController(AutenticacaoService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public SessaoResponse login(@Valid @RequestBody LoginRequest dados) {
        return service.login(dados.login(), dados.senha());
    }
}
