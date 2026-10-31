package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** (sem origem) -> professor: as 1.000 moedas acumulaveis de cada semestre (RN04). */
@Entity
@DiscriminatorValue("CREDITO")
public class CreditoSemestral extends Transacao {

    @Column(length = 7)
    private String semestre;

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
}
