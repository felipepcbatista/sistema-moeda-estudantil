import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { alunosApi, formatarCpf, lerErro } from '../api/api.js'

export default function AlunoLista() {
  const [alunos, setAlunos] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)

  const carregar = () => {
    setCarregando(true)
    alunosApi
      .listar()
      .then(setAlunos)
      .catch((e) => setErro(lerErro(e).mensagem))
      .finally(() => setCarregando(false))
  }

  useEffect(carregar, [])

  const excluir = async (aluno) => {
    if (!window.confirm(`Excluir o aluno ${aluno.nome}?`)) return
    try {
      await alunosApi.excluir(aluno.id)
      carregar()
    } catch (e) {
      setErro(lerErro(e).mensagem)
    }
  }

  return (
    <section>
      <div className="cabecalho">
        <h1>Alunos</h1>
        <Link to="/alunos/novo" className="botao">Novo aluno</Link>
      </div>

      {erro && <p className="alerta">{erro}</p>}

      {carregando ? (
        <p>Carregando…</p>
      ) : alunos.length === 0 ? (
        <p className="vazio">Nenhum aluno cadastrado.</p>
      ) : (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>CPF</th>
                <th>Email</th>
                <th>Instituição</th>
                <th>Curso</th>
                <th className="num">Saldo</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {alunos.map((a) => (
                <tr key={a.id}>
                  <td>{a.nome}</td>
                  <td>{formatarCpf(a.cpf)}</td>
                  <td>{a.email}</td>
                  <td>{a.instituicao.nome}</td>
                  <td>{a.curso}</td>
                  <td className="num">{a.saldo}</td>
                  <td className="acoes">
                    <Link to={`/alunos/${a.id}/editar`}>Editar</Link>
                    <button type="button" className="link-perigo" onClick={() => excluir(a)}>Excluir</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
