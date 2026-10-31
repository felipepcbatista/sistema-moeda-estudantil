package br.pucminas.moedaestudantil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.pucminas.moedaestudantil.model.Conta;

class ContaTest {

    @Test
    void creditaEDebitaRespeitandoSaldo() {
        Conta conta = new Conta();
        conta.creditar(1000);
        conta.debitar(300);

        assertThat(conta.getSaldo()).isEqualTo(700);
        assertThat(conta.possuiSaldo(700)).isTrue();
        assertThat(conta.possuiSaldo(701)).isFalse();
    }

    @Test
    void bloqueiaDebitoMaiorQueSaldoEValorNaoPositivo() {
        Conta conta = new Conta();
        conta.creditar(10);

        assertThatThrownBy(() -> conta.debitar(11)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> conta.creditar(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(conta.getSaldo()).isEqualTo(10);
    }
}
