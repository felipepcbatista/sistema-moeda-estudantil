package br.pucminas.moedaestudantil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.Aluno;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    /** Carrega instituicao e conta junto: a resposta da API e montada fora da transacao (open-in-view=false). */
    @Override
    @EntityGraph(attributePaths = {"instituicao", "conta"})
    Optional<Aluno> findById(Long id);

    @EntityGraph(attributePaths = {"instituicao", "conta"})
    List<Aluno> findAllByOrderByNomeAsc();

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
