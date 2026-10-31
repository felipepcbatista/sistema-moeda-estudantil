package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Professor -> aluno, com motivo obrigatorio (RN05). */
@Entity
@DiscriminatorValue("ENVIO")
public class EnvioMoedas extends Transacao {

    @Column(length = 500)
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
