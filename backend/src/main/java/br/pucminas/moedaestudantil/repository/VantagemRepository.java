package br.pucminas.moedaestudantil.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.Vantagem;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {

    @Override
    @EntityGraph(attributePaths = "empresa")
    Optional<Vantagem> findById(Long id);

    /** Vitrine do aluno (US09): so vantagens ativas. */
    @EntityGraph(attributePaths = "empresa")
    List<Vantagem> findByAtivaTrueOrderByNomeAsc();

    /** Painel da empresa (US06): ativas e desativadas. */
    @EntityGraph(attributePaths = "empresa")
    List<Vantagem> findByEmpresaIdOrderByNomeAsc(Long empresaId);
}
