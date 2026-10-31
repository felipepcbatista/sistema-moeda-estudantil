package br.pucminas.moedaestudantil.dto;

/**
 * Resposta do login: quem entrou, com qual perfil, e o token JWT que o front-end envia em
 * "Authorization: Bearer ..." em todas as requisicoes seguintes.
 */
public record SessaoResponse(Long id, Perfil perfil, String nome, String token) {

    public enum Perfil { ALUNO, PROFESSOR, EMPRESA }
}
