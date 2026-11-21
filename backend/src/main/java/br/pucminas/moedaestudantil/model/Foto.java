package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Arquivo de imagem de uma vantagem (RN08), guardado no banco para sobreviver aos reinicios do servidor.
 * O nome (UUID + extensao) e gerado pelo sistema e define o tipo de conteudo servido.
 */
@Entity
@Table(name = "foto")
public class Foto {

    @Id
    @Column(length = 60)
    private String nome;

    @Column(nullable = false)
    private byte[] dados;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected Foto() {
    }

    public Foto(String nome, byte[] dados) {
        this.nome = nome;
        this.dados = dados;
        this.criadaEm = LocalDateTime.now();
    }

    public String getNome() { return nome; }

    public byte[] getDados() { return dados; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
}
