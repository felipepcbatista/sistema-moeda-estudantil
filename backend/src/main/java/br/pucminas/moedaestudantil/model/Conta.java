package br.pucminas.moedaestudantil.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Saldo de moedas de um Aluno ou Professor; centraliza a regra de saldo suficiente (RN05, RN09). */
@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int saldo;

    public void creditar(int valor) {
        validarValor(valor);
        saldo += valor;
    }

    public void debitar(int valor) {
        validarValor(valor);
        if (!possuiSaldo(valor)) {
            throw new IllegalStateException("Saldo insuficiente");
        }
        saldo -= valor;
    }

    public boolean possuiSaldo(int valor) {
        return saldo >= valor;
    }

    private static void validarValor(int valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
    }

    public Long getId() { return id; }

    public int getSaldo() { return saldo; }
}
