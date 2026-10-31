package br.pucminas.moedaestudantil.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.moedaestudantil.dto.AlunoResumoResponse;
import br.pucminas.moedaestudantil.dto.EnvioMoedasRequest;
import br.pucminas.moedaestudantil.dto.EnvioMoedasResponse;
import br.pucminas.moedaestudantil.dto.ExtratoResponse;
import br.pucminas.moedaestudantil.dto.ProfessorResponse;
import br.pucminas.moedaestudantil.service.ProfessorService;
import br.pucminas.moedaestudantil.service.TransacaoService;
import jakarta.validation.Valid;

/** Tudo aqui e do proprio professor logado: um professor nao envia moedas em nome de outro. */
@RestController
@RequestMapping("/api/professores/{id}")
@PreAuthorize("hasRole('PROFESSOR') and @acesso.ehOProprio(#id)")
public class ProfessorController {

    private final ProfessorService professorService;
    private final TransacaoService transacaoService;

    public ProfessorController(ProfessorService professorService, TransacaoService transacaoService) {
        this.professorService = professorService;
        this.transacaoService = transacaoService;
    }

    @GetMapping
    public ProfessorResponse buscar(@PathVariable Long id) {
        return ProfessorResponse.from(professorService.buscarPorId(id));
    }

    @GetMapping("/alunos")
    public List<AlunoResumoResponse> alunos(@PathVariable Long id) {
        return professorService.listarAlunos(id).stream().map(AlunoResumoResponse::from).toList();
    }

    @PostMapping("/envios")
    @ResponseStatus(HttpStatus.CREATED)
    public EnvioMoedasResponse enviar(@PathVariable Long id, @Valid @RequestBody EnvioMoedasRequest dados) {
        return EnvioMoedasResponse.from(
                transacaoService.enviarMoedas(id, dados.alunoId(), dados.valor(), dados.motivo()));
    }

    @GetMapping("/extrato")
    public ExtratoResponse extrato(@PathVariable Long id) {
        return transacaoService.extratoProfessor(id);
    }
}
