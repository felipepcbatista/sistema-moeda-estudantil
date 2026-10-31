package br.pucminas.moedaestudantil.exception;

import java.util.Map;

/** Corpo padrao das respostas de erro da API; "campos" traz os erros de validacao por campo. */
public record ErroResponse(int status, String mensagem, Map<String, String> campos) {

    public ErroResponse(int status, String mensagem) {
        this(status, mensagem, Map.of());
    }
}
