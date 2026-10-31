import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Campo from '../components/Campo.jsx'
import Modal from '../components/Modal.jsx'
import { alunosApi, authApi, instituicoesApi, lerErro } from '../api/api.js'
import { PAINEL, useSessao } from '../sessao.jsx'

const VAZIO = {
  nome: '',
  email: '',
  cpf: '',
  rg: '',
  curso: '',
  instituicaoId: '',
  login: '',
  senha: '',
  senhaAtual: '',
  endereco: { logradouro: '', numero: '', complemento: '', bairro: '', cidade: '', uf: '', cep: '' },
}

/**
 * CRUD do aluno (US02, US03). Sem id: cadastro publico, que ja entra no sistema ao final.
 * Com id ("Meu cadastro"): consulta, edicao e exclusao da propria conta.
 */
export default function AlunoForm({ id }) {
  const editando = Boolean(id)
  const navegar = useNavigate()
  const { entrar, sair, renomear } = useSessao()

  const [dados, setDados] = useState(VAZIO)
  const [instituicoes, setInstituicoes] = useState([])
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [aviso, setAviso] = useState(null)
  const [salvando, setSalvando] = useState(false)
  const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)

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
            senhaAtual: '',
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
    setAviso(null)
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
      senhaAtual: dados.senhaAtual === '' ? null : dados.senhaAtual,
    }
    try {
      if (editando) {
        const a = await alunosApi.atualizar(id, corpo)
        renomear(a.nome)
        setDados({ ...dados, senha: '', senhaAtual: '' })
        setAviso('Cadastro atualizado.')
      } else {
        await alunosApi.cadastrar(corpo)
        entrar(await authApi.login(corpo.login, corpo.senha))
        navegar(PAINEL.ALUNO)
      }
    } catch (err) {
      setErro(lerErro(err))
    } finally {
      setSalvando(false)
    }
  }

  const excluir = async () => {
    try {
      await alunosApi.excluir(id)
      sair()
      navegar('/')
    } catch (err) {
      setConfirmandoExclusao(false)
      setErro(lerErro(err))
    }
  }

  const c = erro.campos
  return (
    <section className="pagina-form">
      <header className="cabecalho-pagina">
        <h1>{editando ? 'Meu cadastro' : 'Cadastro de aluno'}</h1>
        <p className="suave">
          {editando
            ? 'Mantenha seus dados atualizados.'
            : 'Cadastre-se para receber moedas dos seus professores e trocá-las por vantagens.'}
        </p>
      </header>
      {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}
      {aviso && <p className="sucesso">{aviso}</p>}

      <form onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Dados pessoais</legend>
          <div className="grade">
            <Campo rotulo="Nome completo" nome="nome" value={dados.nome} onChange={alterar} erro={c.nome} largura="l2" />
            <Campo rotulo="Email" nome="email" type="email" value={dados.email} onChange={alterar} erro={c.email} largura="l2" />
            <Campo rotulo="CPF" nome="cpf" value={dados.cpf} onChange={alterar} erro={c.cpf} placeholder="000.000.000-00" largura="l2" />
            <Campo rotulo="RG" nome="rg" value={dados.rg} onChange={alterar} erro={c.rg} largura="l2" />
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
            <Campo rotulo="CEP" nome="cep" value={dados.endereco.cep} onChange={alterarEndereco} erro={c['endereco.cep']} placeholder="00000-000" />
            <Campo rotulo="Logradouro" nome="logradouro" value={dados.endereco.logradouro} onChange={alterarEndereco} erro={c['endereco.logradouro']} largura="l3" />
            <Campo rotulo="Número" nome="numero" value={dados.endereco.numero} onChange={alterarEndereco} erro={c['endereco.numero']} />
            <Campo rotulo="Complemento" nome="complemento" value={dados.endereco.complemento} onChange={alterarEndereco} erro={c['endereco.complemento']} />
            <Campo rotulo="Bairro" nome="bairro" value={dados.endereco.bairro} onChange={alterarEndereco} erro={c['endereco.bairro']} largura="l2" />
            <Campo rotulo="Cidade" nome="cidade" value={dados.endereco.cidade} onChange={alterarEndereco} erro={c['endereco.cidade']} largura="l3" />
            <Campo rotulo="UF" nome="uf" maxLength={2} value={dados.endereco.uf} onChange={alterarEndereco} erro={c['endereco.uf']} />
          </div>
          {c.endereco && <small className="erro-campo">{c.endereco}</small>}
        </fieldset>

        <fieldset>
          <legend>Acesso</legend>
          <div className="grade">
            <Campo rotulo="Login" nome="login" autoComplete="username" value={dados.login} onChange={alterar} erro={c.login} largura="l2" />
            <Campo
              rotulo={editando ? 'Nova senha (deixe em branco para manter)' : 'Senha'}
              nome="senha"
              type="password"
              autoComplete="new-password"
              value={dados.senha}
              onChange={alterar}
              erro={c.senha}
              largura="l2"
            />
            {editando && dados.senha !== '' && (
              <Campo
                rotulo="Senha atual (para confirmar a troca)"
                nome="senhaAtual"
                type="password"
                autoComplete="current-password"
                value={dados.senhaAtual}
                onChange={alterar}
                largura="l2"
              />
            )}
          </div>
        </fieldset>

        <div className="rodape-form">
          {editando ? (
            <button type="button" className="botao perigo-fantasma" onClick={() => setConfirmandoExclusao(true)}>
              Excluir minha conta
            </button>
          ) : (
            <span className="suave">Já tem conta? <Link to="/entrar">Entrar</Link></span>
          )}
          <button type="submit" className="botao primario" disabled={salvando}>
            {salvando ? 'Salvando…' : editando ? 'Salvar alterações' : 'Criar conta'}
          </button>
        </div>
      </form>

      {confirmandoExclusao && (
        <Modal titulo="Excluir conta" aoFechar={() => setConfirmandoExclusao(false)}>
          <p>Sua conta e seus dados serão removidos. Contas que já receberam ou trocaram moedas não podem ser excluídas, para preservar o histórico.</p>
          <div className="modal-acoes">
            <button type="button" className="botao claro" onClick={() => setConfirmandoExclusao(false)}>Cancelar</button>
            <button type="button" className="botao perigo" onClick={excluir}>Excluir conta</button>
          </div>
        </Modal>
      )}
    </section>
  )
}
