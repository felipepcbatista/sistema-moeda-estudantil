package br.pucminas.moedaestudantil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    /** Extrato (US08): transacoes em que a conta aparece como origem ou destino, mais recentes primeiro. */
    @Query("""
            select t from Transacao t
            where t.contaOrigem.id = :contaId or t.contaDestino.id = :contaId
            order by t.dataHora desc, t.id desc
            """)
    List<Transacao> findExtrato(@Param("contaId") Long contaId);

    @Query("""
            select count(t) > 0 from Transacao t
            where t.contaOrigem.id = :contaId or t.contaDestino.id = :contaId
            """)
    boolean existsMovimentacao(@Param("contaId") Long contaId);

    @Query("""
            select count(r) > 0 from ResgateVantagem r
            where r.vantagem.empresa.id = :empresaId
            """)
    boolean existsResgateDaEmpresa(@Param("empresaId") Long empresaId);

    @Query("""
            select r from ResgateVantagem r join fetch r.vantagem v
            where v.empresa.id = :empresaId
            order by r.dataHora desc, r.id desc
            """)
    List<ResgateVantagem> findResgatesDaEmpresa(@Param("empresaId") Long empresaId);

    @Query("select count(r) > 0 from ResgateVantagem r where r.codigo = :codigo")
    boolean existsCodigoResgate(@Param("codigo") String codigo);

    /** Torna o credito semestral idempotente: o job pode rodar de novo sem creditar duas vezes. */
    @Query("""
            select count(c) > 0 from CreditoSemestral c
            where c.contaDestino.id = :contaId and c.semestre = :semestre
            """)
    boolean existsCreditoSemestre(@Param("contaId") Long contaId, @Param("semestre") String semestre);
}
