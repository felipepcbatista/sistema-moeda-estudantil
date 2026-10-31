package br.pucminas.moedaestudantil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.EmpresaParceira;

public interface EmpresaParceiraRepository extends JpaRepository<EmpresaParceira, Long> {

    List<EmpresaParceira> findAllByOrderByNomeFantasiaAsc();

    boolean existsByCnpj(String cnpj);

    boolean existsByCnpjAndIdNot(String cnpj, Long id);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);
}
