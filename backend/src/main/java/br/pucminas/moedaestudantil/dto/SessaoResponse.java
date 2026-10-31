package br.pucminas.moedaestudantil.dto;

/** Quem entrou e com qual perfil; o front-end usa o perfil para montar o menu. */
public record SessaoResponse(Long id, Perfil perfil, String nome) {

    public enum Perfil { ALUNO, PROFESSOR, EMPRESA }
}
