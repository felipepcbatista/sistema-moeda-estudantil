import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Campo from '../components/Campo.jsx'
import { alunosApi, instituicoesApi, lerErro } from '../api/api.js'

const VAZIO = {
  nome: '',
  email: '',
  cpf: '',
  rg: '',
  curso: '',
  instituicaoId: '',
  login: '',
  senha: '',
  endereco: { logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '', cep: '' },
}

export default function AlunoForm() {
  const { id } = useParams()
  const editando = Boolean(id)
  const navegar = useNavigate()

  const [dados, setDados] = useState(VAZIO)
  const [instituicoes, setInstituicoes] = useState([])
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    instituicoesApi.listar().then(setInstituicoes).catch((e) => setErro(lerErro(e)))
    if (editando) {
      alunosApi
        .buscar(id)
        .then((a) =>
          setDados({
            ...a,
            instituicaoId: a.instituicao.id,
            senha: '',
            endereco: { ...a.endereco, complemento: a.endereco.complemento ?? '' },
          }),
        )
        .catch((e) => setErro(lerErro(e)))
    }
  }, [id, editando])

  const alterar = (e) => setDados({ ...dados, [e.target.name]: e.target.value })
  const alterarEndereco = (e) =>
    setDados({ ...dados, endereco: { ...dados.endereco, [e.target.name]: e.target.value } })

  const salvar = async (e) => {
    e.preventDefault()
    setSalvando(true)
    setErro({ mensagem: null, campos: {} })
    const corpo = {
      nome: dados.nome,
      email: dados.email,
      cpf: dados.cpf,
      rg: dados.rg,
      curso: dados.curso,
      endereco: dados.endereco,
      instituicaoId: dados.instituicaoId === '' ? null : Number(dados.instituicaoId),
      login: dados.login,
      senha: dados.senha === '' ? null : dados.senha,
    }
    try {
      if (editando) await alunosApi.atualizar(id, corpo)
      else await alunosApi.cadastrar(corpo)
      navegar('/alunos')
    } catch (err) {
      setErro(lerErro(err))
    } finally {
      setSalvando(false)
    }
  }

  const c = erro.campos
  return (
    <section>
      <h1>{editando ? 'Editar aluno' : 'Cadastro de aluno'}</h1>
      {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}

      <form onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Dados pessoais</legend>
          <div className="grade">
            <Campo rotulo="Nome" nome="nome" value={dados.nome} onChange={alterar} erro={c.nome} largura="l2" />
            <Campo rotulo="Email" nome="email" type="email" value={dados.email} onChange={alterar} erro={c.email} largura="l2" />
            <Campo rotulo="CPF" nome="cpf" value={dados.cpf} onChange={alterar} erro={c.cpf} placeholder="000.000.000-00" />
            <Campo rotulo="RG" nome="rg" value={dados.rg} onChange={alterar} erro={c.rg} />
          </div>
        </fieldset>

        <fieldset>
          <legend>Vínculo acadêmico</legend>
          <div className="grade">
            <Campo rotulo="Instituição de ensino" erro={c.instituicaoId} largura="l2">
              <select name="instituicaoId" value={dados.instituicaoId} onChange={alterar}>
                <option value="">Selecione…</option>
                {instituicoes.map((i) => (
                  <option key={i.id} value={i.id}>{i.nome}</option>
                ))}
              </select>
            </Campo>
            <Campo rotulo="Curso" nome="curso" value={dados.curso} onChange={alterar} erro={c.curso} largura="l2" />
          </div>
        </fieldset>

        <fieldset>
          <legend>Endereço</legend>
          <div className="grade">
            <Campo rotulo="Logradouro" nome="logradouro" value={dados.endereco.logradouro} onChange={alterarEndereco} erro={c['endereco.logradouro']} largura="l3" />
            <Campo rotulo="Número" nome="numero" value={dados.endereco.numero} onChange={alterarEndereco} erro={c['endereco.numero']} />
            <Campo rotulo="Complemento" nome="complemento" value={dados.endereco.complemento} onChange={alterarEndereco} erro={c['endereco.complemento']} />
            <Campo rotulo="Bairro" nome="bairro" value={dados.endereco.bairro} onChange={alterarEndereco} erro={c['endereco.bairro']} />
            <Campo rotulo="Cidade" nome="cidade" value={dados.endereco.cidade} onChange={alterarEndereco} erro={c['endereco.cidade']} />
            <Campo rotulo="UF" nome="uf" maxLength={2} value={dados.endereco.uf} onChange={alterarEndereco} erro={c['endereco.uf']} />
            <Campo rotulo="CEP" nome="cep" value={dados.endereco.cep} onChange={alterarEndereco} erro={c['endereco.cep']} placeholder="00000-000" />
          </div>
          {c.endereco && <small className="erro-campo">{c.endereco}</small>}
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

        <div className="rodape-form">
          <Link to="/alunos" className="botao secundario">Cancelar</Link>
          <button type="submit" className="botao" disabled={salvando}>
            {salvando ? 'Salvando…' : 'Salvar'}
          </button>
        </div>
      </form>
    </section>
  )
}
