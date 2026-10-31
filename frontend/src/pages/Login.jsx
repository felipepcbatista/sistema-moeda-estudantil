import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Campo from '../components/Campo.jsx'
import Moeda from '../components/Moeda.jsx'
import { authApi, lerErro } from '../api/api.js'
import { PAINEL, useSessao } from '../sessao.jsx'

// Contas da carga inicial e do script de demonstracao (scripts/popular-demo.mjs)
const DEMO = [
  ['Professora', 'prof.ana', 'prof123'],
  ['Aluna', 'maria.silva', 'aluno123'],
  ['Empresa', 'cantina.coracao', 'empresa123'],
]

export default function Login() {
  const { entrar } = useSessao()
  const navegar = useNavigate()
  const expirou = useLocation().state?.expirou
  const [dados, setDados] = useState({ login: '', senha: '' })
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [enviando, setEnviando] = useState(false)

  const alterar = (e) => setDados({ ...dados, [e.target.name]: e.target.value })

  const submeter = async (e) => {
    e.preventDefault()
    setEnviando(true)
    setErro({ mensagem: null, campos: {} })
    try {
      const sessao = await authApi.login(dados.login, dados.senha)
      entrar(sessao)
      navegar(PAINEL[sessao.perfil])
    } catch (err) {
      setErro(lerErro(err))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="auth">
      <div className="auth-cartao">
        <Moeda tamanho={44} />
        <h1>Entrar</h1>
        <p className="suave">Professores, alunos e empresas parceiras.</p>
        {expirou && !erro.mensagem && <p className="aviso">Sua sessão expirou. Entre novamente.</p>}
        {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}

        <form onSubmit={submeter} noValidate className="pilha">
          <Campo rotulo="Login" nome="login" autoComplete="username" autoFocus value={dados.login} onChange={alterar} erro={erro.campos.login} />
          <Campo rotulo="Senha" nome="senha" type="password" autoComplete="current-password" value={dados.senha} onChange={alterar} erro={erro.campos.senha} />
          <button type="submit" className="botao primario largo" disabled={enviando}>{enviando ? 'Entrando…' : 'Entrar'}</button>
        </form>

        <p className="suave centro">
          Não tem conta? <Link to="/cadastro/aluno">Sou aluno</Link> · <Link to="/cadastro/empresa">Sou empresa</Link>
          <br />Professores já vêm cadastrados pela instituição.
        </p>
      </div>

      <div className="demo">
        <span>Contas de demonstração</span>
        {DEMO.map(([papel, login, senha]) => (
          <button key={login} type="button" className="demo-conta" onClick={() => setDados({ login, senha })}>
            <b>{papel}</b> {login}
          </button>
        ))}
      </div>
    </section>
  )
}
