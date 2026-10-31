package br.pucminas.moedaestudantil.service;

import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.ResgateVantagem;

/** Interface para que o envio real (email) possa ser trocado ou simulado nos testes. */
public interface NotificacaoService {

    /** RN06: aluno avisado de que recebeu moedas. */
    void notificarRecebimento(EnvioMoedas envio);

    /** RN10: cupom com o codigo, para o aluno usar na troca presencial. */
    void enviarCupomAluno(ResgateVantagem resgate);

    /** RN10: aviso a empresa parceira com o mesmo codigo, para conferencia. */
    void notificarParceiro(ResgateVantagem resgate);
}
