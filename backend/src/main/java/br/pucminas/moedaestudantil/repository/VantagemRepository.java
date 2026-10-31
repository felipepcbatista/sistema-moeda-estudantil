package br.pucminas.moedaestudantil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.Vantagem;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {

    List<Vantagem> findByAtivaTrueOrderByNomeAsc();
}
