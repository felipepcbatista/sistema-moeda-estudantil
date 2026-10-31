package br.pucminas.moedaestudantil.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.Professor;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    @Override
    @EntityGraph(attributePaths = {"instituicao", "conta"})
    Optional<Professor> findById(Long id);

    Optional<Professor> findByContaId(Long contaId);
}
