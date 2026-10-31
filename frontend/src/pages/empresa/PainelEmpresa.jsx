import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { Valor } from '../../components/Moeda.jsx'
import { empresasApi, lerErro, vantagensApi } from '../../api/api.js'
import { dataHora, moedas } from '../../formatar.js'
import { useSessao } from '../../sessao.jsx'

/** US06 + RN10 da empresa: conferir codigos de resgate e gerenciar as vantagens oferecidas. */
export default function PainelEmpresa() {
  const { sessao } = useSessao()
  const [vantagens, setVantagens] = useState(null)
  const [resgates, setResgates] = useState(null)
  const [codigo, setCodigo] = useState('')
  const [erro, setErro] = useState(null)

  const carregar = useCallback(() => {
    Promise.all([vantagensApi.daEmpresa(sessao.id), empresasApi.resgates(sessao.id)])
      .then(([v, r]) => {
        setVantagens(v)
        setResgates(r)
      })
      .catch((e) => setErro(lerErro(e).mensagem))
  }, [sessao])

  useEffect(carregar, [carregar])

  const alternar = async (v) => {
    try {
      if (v.ativa) await vantagensApi.desativar(v.id)
      else await vantagensApi.reativar(v.id)
      carregar()
    } catch (e) {
      setErro(lerErro(e).mensagem)
    }
  }

  if (!vantagens || !resgates) return erro ? <p className="alerta">{erro}</p> : <p className="carregando">Carregando…</p>

  const codigoLimpo = codigo.replace(/[^a-z0-9]/gi, '').toUpperCase()
  const conferido = codigoLimpo.length === 8 ? resgates.find((r) => r.codigo === codigoLimpo) : undefined
  const ativas = vantagens.filter((v) => v.ativa).length
  const moedasTrocadas = resgates.reduce((s, r) => s + r.valor, 0)

  return (
    <>
      <section className="painel-topo">
        <div>
          <span className="sobretitulo">Empresa parceira</span>
          <h1>{sessao.nome}</h1>
          <p className="suave">Confira os códigos apresentados pelos alunos e gerencie as vantagens que você oferece.</p>
        </div>
        <div className="conferir">
          <label htmlFor="codigo">Conferir código de resgate</label>
          <input
            id="codigo"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
            placeholder="Ex.: K7Q2MXRA"
            maxLength={12}
            autoComplete="off"
          />
          {codigoLimpo.length === 8 &&
            (conferido ? (
              <div className="conferido ok">
                <strong>Código válido</strong>
                <span>{conferido.vantagem} · {conferido.aluno}</span>
                <small>Resgatado em {dataHora(conferido.dataHora)}</small>
              </div>
            ) : (
              <div className="conferido falha"><strong>Código não encontrado</strong><span>Confira se foi digitado corretamente.</span></div>
            ))}
        </div>
      </section>

      <section className="indicadores">
        <div><strong>{ativas}</strong><span>vantagens ativas</span></div>
        <div><strong>{resgates.length}</strong><span>resgates</span></div>
        <div><strong>{moedas(moedasTrocadas)}</strong><span>moedas trocadas</span></div>
      </section>

      {erro && <p className="alerta">{erro}</p>}

      <section className="secao">
        <div className="cabecalho-secao">
          <h2 className="titulo-secao">Suas vantagens</h2>
          <Link to="/empresa/vantagens/nova" className="botao primario">Nova vantagem</Link>
        </div>
        {vantagens.length === 0 ? (
          <div className="vazio-cartao">
            <p>Você ainda não oferece nenhuma vantagem.</p>
            <Link to="/empresa/vantagens/nova" className="botao ouro">Cadastrar a primeira</Link>
          </div>
        ) : (
          <div className="vitrine">
            {vantagens.map((v) => (
              <article key={v.id} className={`produto ${v.ativa ? '' : 'inativo'}`}>
                <div className="produto-foto">
                  <img src={v.fotoUrl} alt={v.nome} loading="lazy" />
                  <span className={`selo ${v.ativa ? 'ativo' : ''}`}>{v.ativa ? 'Ativa' : 'Desativada'}</span>
                </div>
                <div className="produto-corpo">
                  <h3>{v.nome}</h3>
                  <p>{v.descricao}</p>
                </div>
                <div className="produto-rodape">
                  <Valor valor={v.custoMoedas} className="preco" />
                  <span className="acoes">
                    <Link to={`/empresa/vantagens/${v.id}/editar`}>Editar</Link>
                    <button type="button" className={v.ativa ? 'link-perigo' : 'link'} onClick={() => alternar(v)}>
                      {v.ativa ? 'Desativar' : 'Reativar'}
                    </button>
                  </span>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="secao">
        <h2 className="titulo-secao">Resgates recebidos</h2>
        {resgates.length === 0 ? (
          <p className="vazio">Nenhum aluno resgatou suas vantagens ainda.</p>
        ) : (
          <div className="bloco sem-padding">
            <table>
              <thead>
                <tr><th>Código</th><th>Vantagem</th><th>Aluno</th><th>Data</th><th className="num">Moedas</th></tr>
              </thead>
              <tbody>
                {resgates.map((r) => (
                  <tr key={r.id} className={conferido?.id === r.id ? 'destacada' : ''}>
                    <td><code className="codigo">{r.codigo}</code></td>
                    <td>{r.vantagem}</td>
                    <td>{r.aluno}</td>
                    <td>{dataHora(r.dataHora)}</td>
                    <td className="num">{moedas(r.valor)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
