package br.pucminas.moedaestudantil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.InstituicaoEnsino;

public interface InstituicaoEnsinoRepository extends JpaRepository<InstituicaoEnsino, Long> {

    List<InstituicaoEnsino> findAllByOrderByNomeAsc();
}
