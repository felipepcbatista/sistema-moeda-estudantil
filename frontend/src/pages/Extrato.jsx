import { useEffect, useState } from 'react'
import { extratoAlunoApi, lerErro, professoresApi } from '../api/api.js'
import Moeda from '../components/Moeda.jsx'
import { dataHora, moedas } from '../formatar.js'
import { useSessao } from '../sessao.jsx'

const TIPOS = {
  ENVIO: { rotulo: 'Reconhecimento', icone: '★' },
  RESGATE: { rotulo: 'Resgate', icone: '⇄' },
  CREDITO: { rotulo: 'Crédito semestral', icone: '+' },
}

/** US08: o mesmo extrato serve professor e aluno; o back-end ja manda cada linha do ponto de vista de quem consulta. */
export default function Extrato() {
  const { sessao } = useSessao()
  const [extrato, setExtrato] = useState(null)
  const [erro, setErro] = useState(null)

  useEffect(() => {
    const buscar = sessao.perfil === 'PROFESSOR' ? professoresApi.extrato : extratoAlunoApi
    buscar(sessao.id).then(setExtrato).catch((e) => setErro(lerErro(e).mensagem))
  }, [sessao])

  if (erro) return <p className="alerta">{erro}</p>
  if (!extrato) return <p className="carregando">Carregando…</p>

  const entradas = extrato.transacoes.filter((t) => t.valor > 0).reduce((s, t) => s + t.valor, 0)
  const saidas = extrato.transacoes.filter((t) => t.valor < 0).reduce((s, t) => s - t.valor, 0)

  return (
    <>
      <header className="cabecalho-pagina">
        <h1>Extrato</h1>
        <p className="suave">Todas as movimentações da sua conta, da mais recente para a mais antiga.</p>
      </header>

      <section className="indicadores">
        <div className="destaque"><strong><Moeda tamanho={26} /> {moedas(extrato.saldo)}</strong><span>saldo atual</span></div>
        <div><strong className="positivo">+{moedas(entradas)}</strong><span>entradas</span></div>
        <div><strong className="negativo">−{moedas(saidas)}</strong><span>saídas</span></div>
        <div><strong>{extrato.transacoes.length}</strong><span>movimentações</span></div>
      </section>

      {extrato.transacoes.length === 0 ? (
        <p className="vazio">Nenhuma movimentação ainda.</p>
      ) : (
        <ul className="extrato">
          {extrato.transacoes.map((t) => (
            <li key={t.id}>
              <span className={`extrato-icone ${t.tipo.toLowerCase()}`} aria-hidden="true">{TIPOS[t.tipo].icone}</span>
              <div className="extrato-info">
                <strong>{t.descricao}</strong>
                {t.motivo && <p>“{t.motivo}”</p>}
                {t.codigo && <p>Código do cupom: <code className="codigo">{t.codigo}</code></p>}
                <small>{TIPOS[t.tipo].rotulo} · {dataHora(t.dataHora)}</small>
              </div>
              <span className={`extrato-valor ${t.valor < 0 ? 'negativo' : 'positivo'}`}>
                {t.valor > 0 ? '+' : '−'}{moedas(Math.abs(t.valor))}
              </span>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
