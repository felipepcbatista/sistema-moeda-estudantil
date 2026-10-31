package br.pucminas.moedaestudantil.model;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

/**
 * Credenciais comuns a Aluno, Professor e EmpresaParceira.
 * Mapeada com JOINED: tabela "usuario" + uma tabela por subclasse, ligadas pela PK.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false, unique = true, length = 60)
    protected String login;

    @Column(name = "senha_hash", nullable = false, length = 100)
    protected String senhaHash;

    /** Confere a senha informada com o hash gravado; o algoritmo (BCrypt) vem de fora da entidade. */
    public boolean autenticar(String senha, PasswordEncoder encoder) {
        return senha != null && encoder.matches(senha, senhaHash);
    }

    public Long getId() { return id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
}
