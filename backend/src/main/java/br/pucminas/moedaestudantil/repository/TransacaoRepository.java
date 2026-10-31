package br.pucminas.moedaestudantil.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.pucminas.moedaestudantil.model.Transacao;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    /** Extrato (US08): transacoes em que a conta aparece como origem ou destino. */
    @Query("""
            select t from Transacao t
            where t.contaOrigem.id = :contaId or t.contaDestino.id = :contaId
            order by t.dataHora desc
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
}
