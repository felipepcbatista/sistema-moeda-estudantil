package br.pucminas.moedaestudantil.exception;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " " + id + " não encontrado(a)");
    }
}
