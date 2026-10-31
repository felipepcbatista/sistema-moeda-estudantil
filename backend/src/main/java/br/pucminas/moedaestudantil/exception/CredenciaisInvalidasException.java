package br.pucminas.moedaestudantil.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Login ou senha inválidos");
    }
}
