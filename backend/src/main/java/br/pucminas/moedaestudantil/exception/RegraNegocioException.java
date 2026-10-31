package br.pucminas.moedaestudantil.exception;

/** Violacao de regra de negocio (dado duplicado, saldo insuficiente, exclusao bloqueada etc.). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
