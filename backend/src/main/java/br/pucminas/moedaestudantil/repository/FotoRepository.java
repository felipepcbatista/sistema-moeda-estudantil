package br.pucminas.moedaestudantil.repository;

import br.pucminas.moedaestudantil.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FotoRepository extends JpaRepository<Foto, String> {
}
