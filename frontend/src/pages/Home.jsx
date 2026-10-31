import { Link } from 'react-router-dom'

export default function Home() {
  return (
    <section>
      <h1>Sistema de Moeda Estudantil</h1>
      <p className="subtitulo">
        Professores reconhecem o mérito dos alunos com moedas; alunos trocam as moedas por vantagens de
        empresas parceiras.
      </p>
      <div className="cartoes">
        <Link to="/alunos" className="cartao">
          <h2>Alunos</h2>
          <p>Cadastrar, consultar, editar e excluir alunos.</p>
        </Link>
        <Link to="/empresas" className="cartao">
          <h2>Empresas parceiras</h2>
          <p>Cadastrar, consultar, editar e excluir empresas parceiras.</p>
        </Link>
      </div>
      <p className="nota">Sprint 2 (Lab03S02): CRUDs de aluno e empresa parceira. Autenticação entra na Sprint 3.</p>
    </section>
  )
}
