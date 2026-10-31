import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Modal from '../../components/Modal.jsx'
import Moeda, { Valor } from '../../components/Moeda.jsx'
import { extratoAlunoApi, lerErro, resgatarApi, vantagensApi } from '../../api/api.js'
import { dataHora, moedas } from '../../formatar.js'
import { useSessao } from '../../sessao.jsx'

/** US08, US09 e US10 do aluno: saldo, reconhecimentos recebidos, vitrine de vantagens e cupons. */
export default function PainelAluno() {
  const { sessao, setSaldo } = useSessao()
  const [extrato, setExtrato] = useState(null)
  const [vantagens, setVantagens] = useState(null)
  const [soAlcancaveis, setSoAlcancaveis] = useState(false)
  const [escolhida, setEscolhida] = useState(null)
  const [cupom, setCupom] = useState(null)
  const [erro, setErro] = useState(null)

  const carregar = useCallback(() => {
    Promise.all([extratoAlunoApi(sessao.id), vantagensApi.ativas()])
      .then(([x, v]) => {
        setExtrato(x)
        setVantagens(v)
        setSaldo(x.saldo)
      })
      .catch((e) => setErro(lerErro(e).mensagem))
  }, [sessao, setSaldo])

  useEffect(carregar, [carregar])

  const recebidos = useMemo(() => extrato?.transacoes.filter((t) => t.tipo === 'ENVIO') ?? [], [extrato])
  const cupons = useMemo(() => extrato?.transacoes.filter((t) => t.tipo === 'RESGATE') ?? [], [extrato])

  if (!extrato || !vantagens) return erro ? <p className="alerta">{erro}</p> : <p className="carregando">Carregando…</p>

  const saldo = extrato.saldo
  const totalRecebido = recebidos.reduce((s, t) => s + t.valor, 0)
  const lista = [...vantagens]
    .filter((v) => !soAlcancaveis || v.custoMoedas <= saldo)
    .sort((a, b) => a.custoMoedas - b.custoMoedas)

  return (
    <>
      <section className="painel-topo">
        <div>
          <span className="sobretitulo">Aluno</span>
          <h1>Olá, {sessao.nome.split(' ')[0]}</h1>
          <p className="suave">
            {totalRecebido > 0
              ? `Você já recebeu ${moedas(totalRecebido)} moedas em ${recebidos.length} reconhecimento(s).`
              : 'Quando um professor reconhecer você, as moedas aparecem aqui.'}
          </p>
        </div>
        <div className="cartao-saldo-grande">
          <span>Seu saldo</span>
          <strong><Moeda tamanho={34} /> {moedas(saldo)}</strong>
          <small><Link to="/extrato">Ver extrato completo →</Link></small>
        </div>
      </section>

      {erro && <p className="alerta">{erro}</p>}

      {recebidos.length > 0 && (
        <section className="secao">
          <h2 className="titulo-secao">Reconhecimentos recebidos</h2>
          <div className="faixa-reconhecimentos">
            {recebidos.slice(0, 4).map((t) => (
              <article key={t.id} className="reconhecimento">
                <div className="feed-topo">
                  <span>{t.descricao.replace('Recebido de ', 'Prof. ')}</span>
                  <Valor valor={t.valor} className="positivo" tamanho={14} />
                </div>
                <p>“{t.motivo}”</p>
                <small>{dataHora(t.dataHora)}</small>
              </article>
            ))}
          </div>
        </section>
      )}

      <section className="secao">
        <div className="cabecalho-secao">
          <h2 className="titulo-secao">Troque suas moedas</h2>
          <label className="alternador">
            <input type="checkbox" checked={soAlcancaveis} onChange={(e) => setSoAlcancaveis(e.target.checked)} />
            <span>Só o que já posso resgatar</span>
          </label>
        </div>

        {lista.length === 0 ? (
          <p className="vazio">
            {vantagens.length === 0 ? 'Nenhuma vantagem disponível no momento.' : 'Seu saldo ainda não alcança nenhuma vantagem. Continue se destacando!'}
          </p>
        ) : (
          <div className="vitrine">
            {lista.map((v) => {
              const falta = v.custoMoedas - saldo
              const progresso = Math.min(100, Math.round((saldo / v.custoMoedas) * 100))
              return (
                <article key={v.id} className="produto">
                  <div className="produto-foto"><img src={v.fotoUrl} alt={v.nome} loading="lazy" /></div>
                  <div className="produto-corpo">
                    <span className="produto-empresa">{v.empresaNome}</span>
                    <h3>{v.nome}</h3>
                    <p>{v.descricao}</p>
                  </div>
                  <div className="produto-rodape">
                    <Valor valor={v.custoMoedas} className="preco" />
                    {falta > 0 ? (
                      <div className="progresso" title={`Faltam ${moedas(falta)} moedas`}>
                        <div style={{ width: `${progresso}%` }} />
                        <span>faltam {moedas(falta)}</span>
                      </div>
                    ) : (
                      <button type="button" className="botao ouro pequeno" onClick={() => setEscolhida(v)}>Resgatar</button>
                    )}
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>

      {cupons.length > 0 && (
        <section className="secao">
          <h2 className="titulo-secao">Meus cupons</h2>
          <div className="bloco sem-padding">
            <table>
              <thead>
                <tr><th>Vantagem</th><th>Código</th><th>Data</th><th className="num">Moedas</th></tr>
              </thead>
              <tbody>
                {cupons.map((t) => (
                  <tr key={t.id}>
                    <td>{t.descricao.replace('Resgate: ', '')}</td>
                    <td><code className="codigo">{t.codigo}</code></td>
                    <td>{dataHora(t.dataHora)}</td>
                    <td className="num negativo">{moedas(t.valor)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}

      {escolhida && (
        <ModalResgate
          vantagem={escolhida}
          saldo={saldo}
          alunoId={sessao.id}
          aoFechar={() => setEscolhida(null)}
          aoResgatar={(r) => {
            setEscolhida(null)
            setCupom(r)
            carregar()
          }}
        />
      )}

      {cupom && (
        <Modal titulo="Resgate confirmado" aoFechar={() => setCupom(null)}>
          <div className="cupom-grande">
            <span>{cupom.vantagem} · {cupom.empresa}</span>
            <code>{cupom.codigo}</code>
            <p>Apresente este código na empresa para fazer a troca. Ele também foi enviado para o seu email, e a empresa recebeu o mesmo código para conferir.</p>
          </div>
          <div className="modal-acoes">
            <button type="button" className="botao primario" onClick={() => setCupom(null)}>Entendi</button>
          </div>
        </Modal>
      )}
    </>
  )
}

function ModalResgate({ vantagem, saldo, alunoId, aoFechar, aoResgatar }) {
  const [erro, setErro] = useState(null)
  const [enviando, setEnviando] = useState(false)

  const confirmar = async () => {
    setEnviando(true)
    setErro(null)
    try {
      aoResgatar(await resgatarApi(alunoId, vantagem.id))
    } catch (e) {
      setErro(lerErro(e).mensagem)
      setEnviando(false)
    }
  }

  return (
    <Modal titulo="Confirmar resgate" aoFechar={aoFechar}>
      <div className="resumo-resgate">
        <img src={vantagem.fotoUrl} alt="" />
        <div>
          <span className="produto-empresa">{vantagem.empresaNome}</span>
          <strong>{vantagem.nome}</strong>
        </div>
      </div>
      <dl className="conta-resgate">
        <div><dt>Saldo atual</dt><dd>{moedas(saldo)}</dd></div>
        <div><dt>Custo</dt><dd className="negativo">−{moedas(vantagem.custoMoedas)}</dd></div>
        <div className="total"><dt>Saldo após o resgate</dt><dd>{moedas(saldo - vantagem.custoMoedas)}</dd></div>
      </dl>
      {erro && <p className="alerta">{erro}</p>}
      <div className="modal-acoes">
        <button type="button" className="botao claro" onClick={aoFechar}>Cancelar</button>
        <button type="button" className="botao ouro" disabled={enviando} onClick={confirmar}>
          {enviando ? 'Resgatando…' : 'Confirmar resgate'}
        </button>
      </div>
    </Modal>
  )
}
