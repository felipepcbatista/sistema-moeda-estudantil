import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Campo from '../components/Campo.jsx'
import { empresasApi, lerErro } from '../api/api.js'

const VAZIO = { nomeFantasia: '', cnpj: '', email: '', login: '', senha: '' }

export default function EmpresaForm() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navegar = useNavigate()

  const [dados, setDados] = useState(VAZIO)
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    if (editando) {
      empresasApi
        .buscar(id)
        .then((e) => setDados({ ...e, senha: '' }))
        .catch((e) => setErro(lerErro(e)))
    }
  }, [id, editando])

  const alterar = (e) => setDados({ ...dados, [e.target.name]: e.target.value })

  const salvar = async (e) => {
    e.preventDefault()
    setSalvando(true)
    setErro({ mensagem: null, campos: {} })
    const corpo = {
      nomeFantasia: dados.nomeFantasia,
      cnpj: dados.cnpj,
      email: dados.email,
      login: dados.login,
      senha: dados.senha === '' ? null : dados.senha,
    }
    try {
      if (editando) await empresasApi.atualizar(id, corpo)
      else await empresasApi.cadastrar(corpo)
      navegar('/empresas')
    } catch (err) {
      setErro(lerErro(err))
    } finally {
      setSalvando(false)
    }
  }

  const c = erro.campos
  return (
    <section>
      <h1>{editando ? 'Editar empresa parceira' : 'Cadastro de empresa parceira'}</h1>
      {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}

      <form onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Dados da empresa</legend>
          <div className="grade">
            <Campo rotulo="Nome fantasia" nome="nomeFantasia" value={dados.nomeFantasia} onChange={alterar} erro={c.nomeFantasia} largura="l2" />
            <Campo rotulo="CNPJ" nome="cnpj" value={dados.cnpj} onChange={alterar} erro={c.cnpj} placeholder="00.000.000/0000-00" largura="l2" />
            <Campo rotulo="Email" nome="email" type="email" value={dados.email} onChange={alterar} erro={c.email} largura="l4" />
          </div>
        </fieldset>

        <fieldset>
          <legend>Acesso</legend>
          <div className="grade">
            <Campo rotulo="Login" nome="login" value={dados.login} onChange={alterar} erro={c.login} largura="l2" />
            <Campo
              rotulo={editando ? 'Nova senha (em branco = manter)' : 'Senha'}
              nome="senha"
              type="password"
              autoComplete="new-password"
              value={dados.senha}
              onChange={alterar}
              erro={c.senha}
              largura="l2"
            />
          </div>
        </fieldset>

        <p className="nota">O cadastro de vantagens oferecidas pela empresa entra na próxima sprint.</p>

        <div className="rodape-form">
          <Link to="/empresas" className="botao secundario">Cancelar</Link>
          <button type="submit" className="botao" disabled={salvando}>
            {salvando ? 'Salvando…' : 'Salvar'}
          </button>
        </div>
      </form>
    </section>
  )
}
