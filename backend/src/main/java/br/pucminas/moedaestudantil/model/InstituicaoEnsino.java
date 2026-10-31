package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Pre-cadastrada (RN02): carregada pela migracao V2, sem CRUD no sistema. */
@Entity
@Table(name = "instituicao_ensino")
public class InstituicaoEnsino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String nome;

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}
