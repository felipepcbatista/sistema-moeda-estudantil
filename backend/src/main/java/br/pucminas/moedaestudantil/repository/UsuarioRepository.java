package br.pucminas.moedaestudantil.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.moedaestudantil.model.Usuario;

/** Login e unico entre todos os perfis, por isso a checagem e feita na tabela-mae. */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByLogin(String login);

    boolean existsByLoginAndIdNot(String login, Long id);
}
