import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { empresasApi, formatarCnpj, lerErro } from '../api/api.js'

export default function EmpresaLista() {
  const [empresas, setEmpresas] = useState([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState(null)

  const carregar = () => {
    setCarregando(true)
    empresasApi
      .listar()
      .then(setEmpresas)
      .catch((e) => setErro(lerErro(e).mensagem))
      .finally(() => setCarregando(false))
  }

  useEffect(carregar, [])

  const excluir = async (empresa) => {
    if (!window.confirm(`Excluir a empresa ${empresa.nomeFantasia}? As vantagens dela também serão removidas.`)) return
    try {
      await empresasApi.excluir(empresa.id)
      carregar()
    } catch (e) {
      setErro(lerErro(e).mensagem)
    }
  }

  return (
    <section>
      <div className="cabecalho">
        <h1>Empresas parceiras</h1>
        <Link to="/empresas/nova" className="botao">Nova empresa</Link>
      </div>

      {erro && <p className="alerta">{erro}</p>}

      {carregando ? (
        <p>Carregando…</p>
      ) : empresas.length === 0 ? (
        <p className="vazio">Nenhuma empresa cadastrada.</p>
      ) : (
        <div className="tabela-rolagem">
          <table>
            <thead>
              <tr>
                <th>Nome fantasia</th>
                <th>CNPJ</th>
                <th>Email</th>
                <th>Login</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {empresas.map((e) => (
                <tr key={e.id}>
                  <td>{e.nomeFantasia}</td>
                  <td>{formatarCnpj(e.cnpj)}</td>
                  <td>{e.email}</td>
                  <td>{e.login}</td>
                  <td className="acoes">
                    <Link to={`/empresas/${e.id}/editar`}>Editar</Link>
                    <button type="button" className="link-perigo" onClick={() => excluir(e)}>Excluir</button>
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
