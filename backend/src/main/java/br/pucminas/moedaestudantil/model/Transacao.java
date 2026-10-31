package br.pucminas.moedaestudantil.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Movimentacao entre contas. Mapeada com SINGLE_TABLE: cada subclasse acrescenta um unico campo,
 * e o extrato (US08) le todas as transacoes de uma conta numa so consulta, sem joins.
 */
@Entity
@Table(name = "transacao")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", length = 20)
public abstract class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(nullable = false)
    protected int valor;

    @Column(name = "data_hora", nullable = false)
    protected LocalDateTime dataHora = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_origem_id")
    protected Conta contaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_destino_id")
    protected Conta contaDestino;

    public Long getId() { return id; }

    public int getValor() { return valor; }
    public void setValor(int valor) { this.valor = valor; }

    public LocalDateTime getDataHora() { return dataHora; }

    public Conta getContaOrigem() { return contaOrigem; }
    public void setContaOrigem(Conta contaOrigem) { this.contaOrigem = contaOrigem; }

    public Conta getContaDestino() { return contaDestino; }
    public void setContaDestino(Conta contaDestino) { this.contaDestino = contaDestino; }
}
