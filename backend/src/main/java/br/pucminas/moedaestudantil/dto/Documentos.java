package br.pucminas.moedaestudantil.dto;

/** CPF, CNPJ e CEP chegam com ou sem mascara e sao gravados so com digitos. */
public final class Documentos {

    private Documentos() {
    }

    public static String somenteDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}
