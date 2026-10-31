import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { definirToken, extratoAlunoApi, professoresApi, quandoSessaoExpirar } from './api/api.js'

// Sessao do usuario logado ({ id, perfil, nome, token }), lembrada no navegador entre recargas.
// O token JWT e quem autoriza cada requisicao no back-end; o perfil aqui so decide o que a tela mostra.
const CHAVE = 'moeda-estudantil.sessao'
const SessaoContext = createContext(null)

export const PAINEL = { PROFESSOR: '/professor', ALUNO: '/aluno', EMPRESA: '/empresa' }

function ler() {
  try {
    const s = JSON.parse(localStorage.getItem(CHAVE))
    return s?.token ? s : null // sessao antiga, sem token, nao vale mais
  } catch {
    return null
  }
}

function gravar(valor) {
  try {
    if (valor) localStorage.setItem(CHAVE, JSON.stringify(valor))
    else localStorage.removeItem(CHAVE)
  } catch {
    // sem armazenamento disponivel: a sessao vale ate recarregar a pagina
  }
}

export function SessaoProvider({ children }) {
  const navegar = useNavigate()
  const [sessao, setSessao] = useState(() => {
    const s = ler()
    definirToken(s?.token ?? null)
    return s
  })
  const [saldo, setSaldo] = useState(null)

  const entrar = useCallback((s) => {
    definirToken(s.token)
    setSessao(s)
    gravar(s)
  }, [])

  const sair = useCallback(() => {
    definirToken(null)
    setSessao(null)
    setSaldo(null)
    gravar(null)
  }, [])

  /** Atualiza o nome exibido (ex.: depois de editar o cadastro) mantendo o token. */
  const renomear = useCallback((nome) => {
    setSessao((s) => {
      const nova = { ...s, nome }
      gravar(nova)
      return nova
    })
  }, [])

  useEffect(() => {
    quandoSessaoExpirar(() => {
      sair()
      navegar('/entrar', { replace: true, state: { expirou: true } })
    })
  }, [sair, navegar])

  // Saldo exibido no cabecalho; as telas chamam setSaldo() depois de enviar ou resgatar moedas.
  const atualizarSaldo = useCallback(() => {
    if (sessao?.perfil === 'PROFESSOR') professoresApi.buscar(sessao.id).then((p) => setSaldo(p.saldo)).catch(() => {})
    else if (sessao?.perfil === 'ALUNO') extratoAlunoApi(sessao.id).then((x) => setSaldo(x.saldo)).catch(() => {})
    else setSaldo(null)
  }, [sessao?.id, sessao?.perfil])

  useEffect(atualizarSaldo, [atualizarSaldo])

  return (
    <SessaoContext.Provider value={{ sessao, entrar, sair, renomear, saldo, setSaldo, atualizarSaldo }}>
      {children}
    </SessaoContext.Provider>
  )
}

export const useSessao = () => useContext(SessaoContext)
