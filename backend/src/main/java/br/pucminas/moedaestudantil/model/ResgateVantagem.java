package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Aluno -> (sem destino), com o codigo de conferencia enviado nos emails (RN10). */
@Entity
@DiscriminatorValue("RESGATE")
public class ResgateVantagem extends Transacao {

    @Column(unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vantagem_id")
    private Vantagem vantagem;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Vantagem getVantagem() { return vantagem; }
    public void setVantagem(Vantagem vantagem) { this.vantagem = vantagem; }
}
