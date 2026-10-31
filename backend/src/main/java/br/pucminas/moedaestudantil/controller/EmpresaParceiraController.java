package br.pucminas.moedaestudantil.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import br.pucminas.moedaestudantil.dto.ResgateEmpresaResponse;
import br.pucminas.moedaestudantil.model.EmpresaParceira;
import br.pucminas.moedaestudantil.service.EmpresaParceiraService;
import br.pucminas.moedaestudantil.service.TransacaoService;
import jakarta.validation.Valid;

/** CRUD da empresa parceira (US04, US05): cadastro publico, o resto so para a propria empresa logada. */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaParceiraController {

    private static final String PROPRIA_EMPRESA = "hasRole('EMPRESA') and @acesso.ehOProprio(#id)";

    private final EmpresaParceiraService service;
    private final TransacaoService transacaoService;

    public EmpresaParceiraController(EmpresaParceiraService service, TransacaoService transacaoService) {
        this.service = service;
        this.transacaoService = transacaoService;
    }

    @PostMapping
    public ResponseEntity<EmpresaParceiraResponse> cadastrar(@Valid @RequestBody EmpresaParceiraRequest dados) {
        EmpresaParceira empresa = service.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/empresas/" + empresa.getId()))
                .body(EmpresaParceiraResponse.from(empresa));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PROPRIA_EMPRESA)
    public EmpresaParceiraResponse buscar(@PathVariable Long id) {
        return EmpresaParceiraResponse.from(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize(PROPRIA_EMPRESA)
    public EmpresaParceiraResponse atualizar(@PathVariable Long id, @Valid @RequestBody EmpresaParceiraRequest dados) {
        return EmpresaParceiraResponse.from(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(PROPRIA_EMPRESA)
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** RN10: resgates das vantagens da empresa, para conferir o codigo na troca presencial. */
    @GetMapping("/{id}/resgates")
    @PreAuthorize(PROPRIA_EMPRESA)
    public List<ResgateEmpresaResponse> resgates(@PathVariable Long id) {
        service.buscarPorId(id);
        return transacaoService.resgatesDaEmpresa(id);
    }
}
