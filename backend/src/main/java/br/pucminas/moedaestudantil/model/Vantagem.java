package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vantagem")
public class Vantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(name = "foto_url", nullable = false, length = 300)
    private String fotoUrl;

    @Column(name = "custo_moedas", nullable = false)
    private int custoMoedas;

    @Column(nullable = false)
    private boolean ativa = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaParceira empresa;

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public int getCustoMoedas() { return custoMoedas; }
    public void setCustoMoedas(int custoMoedas) { this.custoMoedas = custoMoedas; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    public EmpresaParceira getEmpresa() { return empresa; }
    public void setEmpresa(EmpresaParceira empresa) { this.empresa = empresa; }
}
