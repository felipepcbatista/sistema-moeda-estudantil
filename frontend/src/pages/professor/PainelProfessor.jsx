import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Avatar from '../../components/Avatar.jsx'
import Campo from '../../components/Campo.jsx'
import Modal from '../../components/Modal.jsx'
import Moeda, { Valor } from '../../components/Moeda.jsx'
import { lerErro, professoresApi } from '../../api/api.js'
import { data, dataHora, moedas, proximoCredito } from '../../formatar.js'
import { useSessao } from '../../sessao.jsx'

const VALORES_RAPIDOS = [10, 25, 50, 100]
const MOTIVOS_SUGERIDOS = ['Participação em aula', 'Trabalho excelente', 'Ajudou os colegas', 'Bom comportamento']

/** US07 + US08 do professor: saldo, alunos da instituicao e reconhecimentos ja feitos. */
export default function PainelProfessor() {
  const { sessao, setSaldo } = useSessao()
  const [professor, setProfessor] = useState(null)
  const [alunos, setAlunos] = useState([])
  const [extrato, setExtrato] = useState(null)
  const [busca, setBusca] = useState('')
  const [selecionado, setSelecionado] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [erro, setErro] = useState(null)

  const carregar = useCallback(() => {
    Promise.all([professoresApi.buscar(sessao.id), professoresApi.alunos(sessao.id), professoresApi.extrato(sessao.id)])
      .then(([p, a, x]) => {
        setProfessor(p)
        setAlunos(a)
        setExtrato(x)
        setSaldo(p.saldo)
      })
      .catch((e) => setErro(lerErro(e).mensagem))
  }, [sessao, setSaldo])

  useEffect(carregar, [carregar])

  const envios = useMemo(() => extrato?.transacoes.filter((t) => t.tipo === 'ENVIO') ?? [], [extrato])
  const totalEnviado = envios.reduce((s, t) => s - t.valor, 0)
  const filtrados = alunos.filter((a) =>
    `${a.nome} ${a.curso}`.toLowerCase().includes(busca.trim().toLowerCase()),
  )

  if (!professor || !extrato) return erro ? <p className="alerta">{erro}</p> : <p className="carregando">Carregando…</p>

  return (
    <>
      <section className="painel-topo">
        <div>
          <span className="sobretitulo">{professor.instituicao.nome} · {professor.departamento}</span>
          <h1>Olá, {professor.nome.split(' ')[0]}</h1>
          <p className="suave">Reconheça o esforço dos seus alunos enviando moedas com uma mensagem.</p>
        </div>
        <div className="cartao-saldo-grande">
          <span>Seu saldo</span>
          <strong><Moeda tamanho={34} /> {moedas(professor.saldo)}</strong>
          <small>+1.000 moedas em {data(proximoCredito())}</small>
        </div>
      </section>

      <section className="indicadores">
        <div><strong>{moedas(totalEnviado)}</strong><span>moedas enviadas</span></div>
        <div><strong>{envios.length}</strong><span>reconhecimentos</span></div>
        <div><strong>{new Set(envios.map((e) => e.descricao)).size}</strong><span>alunos reconhecidos</span></div>
        <div><strong>{alunos.length}</strong><span>alunos na instituição</span></div>
      </section>

      {aviso && <p className="sucesso">{aviso}</p>}

      <div className="duas-colunas">
        <section className="bloco">
          <div className="bloco-topo">
            <h2>Seus alunos</h2>
            <input className="busca" type="search" placeholder="Buscar por nome ou curso" value={busca} onChange={(e) => setBusca(e.target.value)} />
          </div>
          {alunos.length === 0 ? (
            <p className="vazio">Nenhum aluno da {professor.instituicao.nome} se cadastrou ainda.</p>
          ) : filtrados.length === 0 ? (
            <p className="vazio">Nenhum aluno encontrado para “{busca}”.</p>
          ) : (
            <ul className="lista-pessoas">
              {filtrados.map((a) => (
                <li key={a.id}>
                  <Avatar nome={a.nome} />
                  <div className="pessoa-info">
                    <strong>{a.nome}</strong>
                    <span>{a.curso}</span>
                  </div>
                  <Valor valor={a.saldo} className="suave" />
                  <button type="button" className="botao ouro pequeno" onClick={() => setSelecionado(a)}>
                    Reconhecer
                  </button>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="bloco">
          <div className="bloco-topo">
            <h2>Reconhecimentos recentes</h2>
            <Link to="/extrato">Ver extrato</Link>
          </div>
          {envios.length === 0 ? (
            <p className="vazio">Você ainda não reconheceu nenhum aluno neste semestre.</p>
          ) : (
            <ul className="feed">
              {envios.slice(0, 6).map((t) => (
                <li key={t.id}>
                  <div className="feed-topo">
                    <span>{t.descricao.replace('Enviado para ', '')}</span>
                    <Valor valor={-t.valor} className="positivo" tamanho={14} />
                  </div>
                  <p>“{t.motivo}”</p>
                  <small>{dataHora(t.dataHora)}</small>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>

      {selecionado && (
        <ModalReconhecer
          aluno={selecionado}
          saldo={professor.saldo}
          professorId={sessao.id}
          aoFechar={() => setSelecionado(null)}
          aoEnviar={(envio) => {
            setSelecionado(null)
            setAviso(`${moedas(envio.valor)} moedas enviadas para ${selecionado.nome}. Um email avisou o aluno.`)
            carregar()
          }}
        />
      )}
    </>
  )
}

function ModalReconhecer({ aluno, saldo, professorId, aoFechar, aoEnviar }) {
  const [valor, setValor] = useState('')
  const [motivo, setMotivo] = useState('')
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [enviando, setEnviando] = useState(false)

  const enviar = async (e) => {
    e.preventDefault()
    setEnviando(true)
    setErro({ mensagem: null, campos: {} })
    try {
      aoEnviar(await professoresApi.enviar(professorId, {
        alunoId: aluno.id,
        valor: valor === '' ? null : Number(valor),
        motivo,
      }))
    } catch (err) {
      setErro(lerErro(err))
      setEnviando(false)
    }
  }

  return (
    <Modal titulo="Reconhecer aluno" aoFechar={aoFechar} largura={520}>
      <div className="modal-pessoa">
        <Avatar nome={aluno.nome} tamanho={48} />
        <div>
          <strong>{aluno.nome}</strong>
          <span className="suave">{aluno.curso}</span>
        </div>
      </div>
      {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}

      <form onSubmit={enviar} noValidate className="pilha">
        <div className="campo">
          <span>Quantas moedas?</span>
          <div className="chips">
            {VALORES_RAPIDOS.map((v) => (
              <button key={v} type="button" className={`chip ${Number(valor) === v ? 'ativo' : ''}`} disabled={v > saldo} onClick={() => setValor(String(v))}>
                <Moeda tamanho={14} /> {v}
              </button>
            ))}
            <input className="chip-input" type="number" min="1" max={saldo} placeholder="Outro" value={valor} onChange={(e) => setValor(e.target.value)} aria-label="Quantidade de moedas" />
          </div>
          {erro.campos.valor && <small className="erro-campo">{erro.campos.valor}</small>}
          <small className="suave">Seu saldo: {moedas(saldo)} moedas</small>
        </div>

        <Campo rotulo="Motivo do reconhecimento" erro={erro.campos.motivo}>
          <textarea rows={3} maxLength={500} placeholder="Escreva para o aluno por que ele está sendo reconhecido" value={motivo} onChange={(e) => setMotivo(e.target.value)} />
        </Campo>
        <div className="chips">
          {MOTIVOS_SUGERIDOS.map((m) => (
            <button key={m} type="button" className="chip claro" onClick={() => setMotivo(m)}>{m}</button>
          ))}
        </div>

        <div className="modal-acoes">
          <button type="button" className="botao claro" onClick={aoFechar}>Cancelar</button>
          <button type="submit" className="botao ouro" disabled={enviando}>
            {enviando ? 'Enviando…' : valor ? `Enviar ${moedas(Number(valor))} moedas` : 'Enviar moedas'}
          </button>
        </div>
      </form>
    </Modal>
  )
}
