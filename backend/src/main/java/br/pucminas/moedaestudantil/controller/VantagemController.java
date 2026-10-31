package br.pucminas.moedaestudantil.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.pucminas.moedaestudantil.dto.VantagemRequest;
import br.pucminas.moedaestudantil.dto.VantagemResponse;
import br.pucminas.moedaestudantil.service.VantagemService;
import jakarta.validation.Valid;

/**
 * Cadastro e edicao chegam como multipart: parte "dados" (JSON) + parte "foto" (arquivo de imagem).
 */
@RestController
public class VantagemController {

    private final VantagemService service;

    public VantagemController(VantagemService service) {
        this.service = service;
    }

    /** Vitrine do aluno: so as ativas. */
    @GetMapping("/api/vantagens")
    public List<VantagemResponse> listarAtivas() {
        return service.listarAtivas().stream().map(VantagemResponse::from).toList();
    }

    @GetMapping("/api/vantagens/{id}")
    public VantagemResponse buscar(@PathVariable Long id) {
        return VantagemResponse.from(service.buscarPorId(id));
    }

    @GetMapping("/api/empresas/{empresaId}/vantagens")
    public List<VantagemResponse> listarDaEmpresa(@PathVariable Long empresaId) {
        return service.listarDaEmpresa(empresaId).stream().map(VantagemResponse::from).toList();
    }

    @PostMapping(path = "/api/empresas/{empresaId}/vantagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public VantagemResponse cadastrar(@PathVariable Long empresaId,
                                      @Valid @RequestPart("dados") VantagemRequest dados,
                                      @RequestPart(value = "foto", required = false) MultipartFile foto) {
        return VantagemResponse.from(service.cadastrar(empresaId, dados, foto));
    }

    @PutMapping(path = "/api/vantagens/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public VantagemResponse atualizar(@PathVariable Long id,
                                      @Valid @RequestPart("dados") VantagemRequest dados,
                                      @RequestPart(value = "foto", required = false) MultipartFile foto) {
        return VantagemResponse.from(service.atualizar(id, dados, foto));
    }

    @PostMapping("/api/vantagens/{id}/desativar")
    public VantagemResponse desativar(@PathVariable Long id) {
        return VantagemResponse.from(service.desativar(id));
    }

    @PostMapping("/api/vantagens/{id}/reativar")
    public VantagemResponse reativar(@PathVariable Long id) {
        return VantagemResponse.from(service.reativar(id));
    }
}
