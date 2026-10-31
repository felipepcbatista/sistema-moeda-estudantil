import { NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import { PAINEL, useSessao } from './sessao.jsx'
import Moeda, { Valor } from './components/Moeda.jsx'
import Avatar from './components/Avatar.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import AlunoForm from './pages/AlunoForm.jsx'
import EmpresaForm from './pages/EmpresaForm.jsx'
import Extrato from './pages/Extrato.jsx'
import PainelProfessor from './pages/professor/PainelProfessor.jsx'
import PainelAluno from './pages/aluno/PainelAluno.jsx'
import PainelEmpresa from './pages/empresa/PainelEmpresa.jsx'
import VantagemForm from './pages/empresa/VantagemForm.jsx'

const MENUS = {
  PROFESSOR: [['/professor', 'Reconhecer alunos'], ['/extrato', 'Extrato']],
  ALUNO: [['/aluno', 'Vantagens'], ['/extrato', 'Extrato'], ['/meu-cadastro', 'Meu cadastro']],
  EMPRESA: [['/empresa', 'Painel'], ['/meu-cadastro', 'Meu cadastro']],
}
const PERFIL_ROTULO = { PROFESSOR: 'Professor', ALUNO: 'Aluno', EMPRESA: 'Empresa parceira' }

/** So deixa entrar quem esta logado com o perfil certo. */
function Exige({ perfil, children }) {
  const { sessao } = useSessao()
  if (!sessao) return <Navigate to="/entrar" replace />
  if (perfil && sessao.perfil !== perfil) return <Navigate to={PAINEL[sessao.perfil]} replace />
  return children
}

function MeuCadastro() {
  const { sessao } = useSessao()
  if (!sessao) return <Navigate to="/entrar" replace />
  if (sessao.perfil === 'ALUNO') return <AlunoForm id={sessao.id} />
  if (sessao.perfil === 'EMPRESA') return <EmpresaForm id={sessao.id} />
  return <Navigate to={PAINEL[sessao.perfil]} replace />
}

export default function App() {
  const { sessao, sair, saldo } = useSessao()
  const navegar = useNavigate()

  return (
    <div className="app">
      <header className="topo">
        <div className="topo-interno">
          <NavLink to={sessao ? PAINEL[sessao.perfil] : '/'} className="marca">
            <Moeda tamanho={30} />
            <span>Moeda<b>Estudantil</b></span>
          </NavLink>

          {sessao ? (
            <>
              <nav className="menu">
                {MENUS[sessao.perfil].map(([para, rotulo]) => (
                  <NavLink key={para} to={para} end>{rotulo}</NavLink>
                ))}
              </nav>
              <div className="topo-usuario">
                {saldo !== null && <Valor valor={saldo} className="saldo-pilula" />}
                <Avatar nome={sessao.nome} tamanho={34} />
                <div className="topo-nome">
                  <strong>{sessao.nome}</strong>
                  <span>{PERFIL_ROTULO[sessao.perfil]}</span>
                </div>
                <button
                  type="button"
                  className="botao fantasma pequeno"
                  onClick={() => {
                    sair()
                    navegar('/')
                  }}
                >
                  Sair
                </button>
              </div>
            </>
          ) : (
            <div className="topo-usuario">
              <NavLink to="/cadastro/aluno" className="link-topo">Sou aluno</NavLink>
              <NavLink to="/cadastro/empresa" className="link-topo">Sou empresa</NavLink>
              <NavLink to="/entrar" className="botao ouro pequeno">Entrar</NavLink>
            </div>
          )}
        </div>
      </header>

      <main className="conteudo">
        <Routes>
          <Route path="/" element={sessao ? <Navigate to={PAINEL[sessao.perfil]} replace /> : <Home />} />
          <Route path="/entrar" element={<Login />} />
          <Route path="/cadastro/aluno" element={<AlunoForm />} />
          <Route path="/cadastro/empresa" element={<EmpresaForm />} />
          <Route path="/meu-cadastro" element={<MeuCadastro />} />
          <Route path="/extrato" element={<Exige><Extrato /></Exige>} />
          <Route path="/professor" element={<Exige perfil="PROFESSOR"><PainelProfessor /></Exige>} />
          <Route path="/aluno" element={<Exige perfil="ALUNO"><PainelAluno /></Exige>} />
          <Route path="/empresa" element={<Exige perfil="EMPRESA"><PainelEmpresa /></Exige>} />
          <Route path="/empresa/vantagens/nova" element={<Exige perfil="EMPRESA"><VantagemForm /></Exige>} />
          <Route path="/empresa/vantagens/:id/editar" element={<Exige perfil="EMPRESA"><VantagemForm /></Exige>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>

      <footer className="rodape">
        Sistema de Moeda Estudantil · Laboratório de Desenvolvimento de Software · PUC Minas 2026/2
      </footer>
    </div>
  )
}
