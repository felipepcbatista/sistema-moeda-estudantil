package br.pucminas.moedaestudantil.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "aluno")
@PrimaryKeyJoinColumn(name = "id")
public class Aluno extends Usuario {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 20)
    private String rg;

    @Column(nullable = false, length = 100)
    private String curso;

    @Embedded
    private Endereco endereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    /** Composicao: a conta nasce e morre com o aluno; todo aluno comeca com saldo 0. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "conta_id", nullable = false, unique = true)
    private Conta conta = new Conta();

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getRg() { return rg; }
    public void setRg(String rg) { this.rg = rg; }

    public String getCurso() { return curso; }
    public void setCurso(String curso) { this.curso = curso; }

    public Endereco getEndereco() { return endereco; }
    public void setEndereco(Endereco endereco) { this.endereco = endereco; }

    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }

    public Conta getConta() { return conta; }
}
