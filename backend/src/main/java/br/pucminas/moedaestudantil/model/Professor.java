package br.pucminas.moedaestudantil.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/** Pre-cadastrado pela instituicao (RN03): carregado pela migracao V2. */
@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name = "id")
public class Professor extends Usuario {

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false, length = 100)
    private String departamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instituicao_id", nullable = false)
    private InstituicaoEnsino instituicao;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "conta_id", nullable = false, unique = true)
    private Conta conta = new Conta();

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public InstituicaoEnsino getInstituicao() { return instituicao; }
    public void setInstituicao(InstituicaoEnsino instituicao) { this.instituicao = instituicao; }

    public Conta getConta() { return conta; }
}
