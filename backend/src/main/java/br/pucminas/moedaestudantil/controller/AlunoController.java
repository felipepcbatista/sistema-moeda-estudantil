package br.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;
    private final TransacaoService transacaoService;

    public AlunoController(AlunoService service, TransacaoService transacaoService) {
        this.service = service;
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public List<AlunoResponse> listar() {
        return service.listar().stream().map(AlunoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscar(@PathVariable Long id) {
        return AlunoResponse.from(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<AlunoResponse> cadastrar(@Valid @RequestBody AlunoRequest dados) {
        Aluno aluno = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/alunos/" + aluno.getId())).body(AlunoResponse.from(aluno));
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable Long id, @Valid @RequestBody AlunoRequest dados) {
        return AlunoResponse.from(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/extrato")
    public ExtratoResponse extrato(@PathVariable Long id) {
        return transacaoService.extratoAluno(id);
    }

    @PostMapping("/{id}/resgates")
    @ResponseStatus(HttpStatus.CREATED)
    public ResgateResponse resgatar(@PathVariable Long id, @Valid @RequestBody ResgateRequest dados) {
        return ResgateResponse.from(transacaoService.resgatarVantagem(id, dados.vantagemId()));
    }
}
