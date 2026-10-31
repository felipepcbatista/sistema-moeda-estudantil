package br.pucminas.moedaestudantil.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;
import br.pucminas.moedaestudantil.repository.InstituicaoEnsinoRepository;

/** So leitura: alimenta o select de instituicao no cadastro de aluno (RN02). */
@Service
public class InstituicaoEnsinoService {

    private final InstituicaoEnsinoRepository repository;

    public InstituicaoEnsinoService(InstituicaoEnsinoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<InstituicaoEnsino> listar() {
        return repository.findAllByOrderByNomeAsc();
    }
}
