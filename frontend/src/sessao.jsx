import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { extratoAlunoApi, professoresApi } from './api/api.js'

// Sessao do usuario logado ({ id, perfil, nome }), lembrada no navegador entre recargas.
// Nao e um token de seguranca: a protecao das rotas no back-end (JWT) entra na Sprint 3.
const CHAVE = 'moeda-estudantil.sessao'
const SessaoContext = createContext(null)

export const PAINEL = { PROFESSOR: '/professor', ALUNO: '/aluno', EMPRESA: '/empresa' }

function ler() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE))
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
  const [sessao, setSessao] = useState(ler)
  const [saldo, setSaldo] = useState(null)

  // Saldo exibido no cabecalho; as telas chamam atualizarSaldo() depois de enviar ou resgatar moedas.
  const atualizarSaldo = useCallback(() => {
    if (sessao?.perfil === 'PROFESSOR') professoresApi.buscar(sessao.id).then((p) => setSaldo(p.saldo)).catch(() => {})
    else if (sessao?.perfil === 'ALUNO') extratoAlunoApi(sessao.id).then((x) => setSaldo(x.saldo)).catch(() => {})
    else setSaldo(null)
  }, [sessao])

  useEffect(atualizarSaldo, [atualizarSaldo])

  const entrar = (s) => {
    setSessao(s)
    gravar(s)
  }

  const sair = () => {
    setSessao(null)
    setSaldo(null)
    gravar(null)
  }

  return (
    <SessaoContext.Provider value={{ sessao, entrar, sair, saldo, setSaldo, atualizarSaldo }}>
      {children}
    </SessaoContext.Provider>
  )
}

export const useSessao = () => useContext(SessaoContext)
