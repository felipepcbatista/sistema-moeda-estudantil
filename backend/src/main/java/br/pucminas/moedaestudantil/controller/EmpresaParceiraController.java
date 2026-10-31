package br.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.moedaestudantil.dto.EmpresaParceiraRequest;
import br.pucminas.moedaestudantil.dto.EmpresaParceiraResponse;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.service.EmpresaParceiraService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaParceiraController {

    private final EmpresaParceiraService service;

    public EmpresaParceiraController(EmpresaParceiraService service) {
        this.service = service;
    }

    @GetMapping
    public List<EmpresaParceiraResponse> listar() {
        return service.listar().stream().map(EmpresaParceiraResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EmpresaParceiraResponse buscar(@PathVariable Long id) {
        return EmpresaParceiraResponse.from(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EmpresaParceiraResponse> cadastrar(@Valid @RequestBody EmpresaParceiraRequest dados) {
        EmpresaParceira empresa = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/empresas/" + empresa.getId()))
                .body(EmpresaParceiraResponse.from(empresa));
    }

    @PutMapping("/{id}")
    public EmpresaParceiraResponse atualizar(@PathVariable Long id, @Valid @RequestBody EmpresaParceiraRequest dados) {
        return EmpresaParceiraResponse.from(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
