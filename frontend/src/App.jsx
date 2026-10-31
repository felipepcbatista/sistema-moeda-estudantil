import { NavLink, Route, Routes } from 'react-router-dom'
import Home from './pages/Home.jsx'
import AlunoLista from './pages/AlunoLista.jsx'
import AlunoForm from './pages/AlunoForm.jsx'
import EmpresaLista from './pages/EmpresaLista.jsx'
import EmpresaForm from './pages/EmpresaForm.jsx'

export default function App() {
  return (
    <>
      <header className="topo">
        <NavLink to="/" className="marca">Moeda Estudantil</NavLink>
        <nav>
          <NavLink to="/alunos">Alunos</NavLink>
          <NavLink to="/empresas">Empresas parceiras</NavLink>
        </nav>
      </header>
      <main className="conteudo">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/alunos" element={<AlunoLista />} />
          <Route path="/alunos/novo" element={<AlunoForm />} />
          <Route path="/alunos/:id/editar" element={<AlunoForm />} />
          <Route path="/empresas" element={<EmpresaLista />} />
          <Route path="/empresas/nova" element={<EmpresaForm />} />
          <Route path="/empresas/:id/editar" element={<EmpresaForm />} />
        </Routes>
      </main>
    </>
  )
}
