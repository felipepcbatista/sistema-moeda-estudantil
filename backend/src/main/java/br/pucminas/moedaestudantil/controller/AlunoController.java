package br.pucminas.moedaestudantil.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.moedaestudantil.dto.AlunoRequest;
import br.pucminas.moedaestudantil.dto.AlunoResponse;
import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ResgateRequest;
import br.pucminas.moedaestudantil.dto.ResgateResponse;
import br.pucminas.moedaestudantil.model.Aluno;
import br.pucminas.moedaestudantil.service.AlunoService;
import br.pucminas.moedaestudantil.service.TransacaoService;
import jakarta.validation.Valid;

/**
 * CRUD do aluno (US02, US03). O cadastro e publico; consulta, edicao, exclusao, extrato e resgate
 * so para o proprio aluno logado. Nao ha listagem de todos os alunos: exporia dados pessoais de terceiros.
 */
@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private static final String PROPRIO_ALUNO = "hasRole('ALUNO') and @acesso.ehOProprio(#id)";

    private final AlunoService service;
    private final TransacaoService transacaoService;

    public AlunoController(AlunoService service, TransacaoService transacaoService) {
        this.service = service;
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest dados) {
        Aluno aluno = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/alunos/" + aluno.getId())).body(AlunoResponse.from(aluno));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PROPRIO_ALUNO)
    public AlunoResponse buscar(@PathVariable Long id) {
        return AlunoResponse.from(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PROPRIO_ALUNO)
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest dados) {
        return AlunoResponse.from(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PROPRIO_ALUNO)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/extrato")
    @PreAuthorize(PROPRIO_ALUNO)
    public ExtratoResponse extrato(@PathVariable Long id) {
        return transacaoService.extratoAluno(id);
    }

    @PostMapping("/{id}/resgates")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PROPRIO_ALUNO)
    public ResgateResponse resgatar(@PathVariable Long id, @Valid @RequestBody ResgateRequest dados) {
        return ResgateResponse.from(transacaoService.resgatarVantagem(id, dados.vantagemId()));
    }
}
