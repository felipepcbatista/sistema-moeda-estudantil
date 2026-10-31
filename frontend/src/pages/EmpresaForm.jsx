import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Campo from '../components/Campo.jsx'
import Modal from '../components/Modal.jsx'
import { authApi, empresasApi, lerErro } from '../api/api.js'
import { PAINEL, useSessao } from '../sessao.jsx'

const VAZIO = { nomeFantasia: '', cnpj: '', email: '', login: '', senha: '', senhaAtual: '' }

/** CRUD da empresa parceira (US04, US05), com o mesmo comportamento do AlunoForm. */
export default function EmpresaForm({ id }) {
  const editando = Boolean(id)
  const navegar = useNavigate()
  const { entrar, sair, renomear } = useSessao()

  const [dados, setDados] = useState(VAZIO)
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [aviso, setAviso] = useState(null)
  const [salvando, setSalvando] = useState(false)
  const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)

  useEffect(() => {
    if (editando) {
      empresasApi
        .buscar(id)
        .then((e) => setDados({ ...e, senha: '', senhaAtual: '' }))
        .catch((e) => setErro(lerErro(e)))
    }
  }, [id, editando])

  const alterar = (e) => setDados({ ...dados, [e.target.name]: e.target.value })

  const salvar = async (e) => {
    e.preventDefault()
    setSalvando(true)
    setErro({ mensagem: null, campos: {} })
    setAviso(null)
    const corpo = {
      nomeFantasia: dados.nomeFantasia,
      cnpj: dados.cnpj,
      email: dados.email,
      login: dados.login,
      senha: dados.senha === '' ? null : dados.senha,
      senhaAtual: dados.senhaAtual === '' ? null : dados.senhaAtual,
    }
    try {
      if (editando) {
        const emp = await empresasApi.atualizar(id, corpo)
        renomear(emp.nomeFantasia)
        setDados({ ...dados, senha: '', senhaAtual: '' })
        setAviso('Cadastro atualizado.')
      } else {
        await empresasApi.cadastrar(corpo)
        entrar(await authApi.login(corpo.login, corpo.senha))
        navegar(PAINEL.EMPRESA)
      }
    } catch (err) {
      setErro(lerErro(err))
    } finally {
      setSalvando(false)
    }
  }

  const excluir = async () => {
    try {
      await empresasApi.excluir(id)
      sair()
      navegar('/')
    } catch (err) {
      setConfirmandoExclusao(false)
      setErro(lerErro(err))
    }
  }

  const c = erro.campos
  return (
    <section className="pagina-form estreita">
      <header className="cabecalho-pagina">
        <h1>{editando ? 'Meu cadastro' : 'Cadastro de empresa parceira'}</h1>
        <p className="suave">
          {editando
            ? 'Dados da empresa e de acesso.'
            : 'Ofereça descontos e produtos aos alunos. Eles pagam com as moedas que recebem dos professores.'}
        </p>
      </header>
      {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}
      {aviso && <p className="sucesso">{aviso}</p>}

      <form onSubmit={salvar} noValidate>
        <fieldset>
          <legend>Empresa</legend>
          <div className="grade">
            <Campo rotulo="Nome fantasia" nome="nomeFantasia" value={dados.nomeFantasia} onChange={alterar} erro={c.nomeFantasia} largura="l4" />
            <Campo rotulo="CNPJ" nome="cnpj" value={dados.cnpj} onChange={alterar} erro={c.cnpj} placeholder="00.000.000/0000-00" largura="l2" />
            <Campo rotulo="Email (recebe os avisos de resgate)" nome="email" type="email" value={dados.email} onChange={alterar} erro={c.email} largura="l2" />
          </div>
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
              Excluir conta da empresa
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
        <Modal titulo="Excluir conta da empresa" aoFechar={() => setConfirmandoExclusao(false)}>
          <p>A empresa e todas as suas vantagens serão removidas. Empresas com vantagens já resgatadas não podem ser excluídas, para preservar os cupons emitidos.</p>
          <div className="modal-acoes">
            <button type="button" className="botao claro" onClick={() => setConfirmandoExclusao(false)}>Cancelar</button>
            <button type="button" className="botao perigo" onClick={excluir}>Excluir conta</button>
          </div>
        </Modal>
      )}
    </section>
  )
}
