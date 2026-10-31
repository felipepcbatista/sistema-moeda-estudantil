import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Campo from '../../components/Campo.jsx'
import { Valor } from '../../components/Moeda.jsx'
import { lerErro, vantagensApi } from '../../api/api.js'
import { useSessao } from '../../sessao.jsx'

/** US06 / RN08: nome, descricao, foto do produto e custo em moedas, com previa de como o aluno vera. */
export default function VantagemForm() {
  const { id } = useParams()
  const editando = Boolean(id)
  const { sessao } = useSessao()
  const navegar = useNavigate()

  const [dados, setDados] = useState({ nome: '', descricao: '', custoMoedas: '' })
  const [foto, setFoto] = useState(null)
  const [previa, setPrevia] = useState(null)
  const [erro, setErro] = useState({ mensagem: null, campos: {} })
  const [salvando, setSalvando] = useState(false)

  useEffect(() => {
    if (editando) {
      vantagensApi
        .buscar(id)
        .then((v) => {
          setDados({ nome: v.nome, descricao: v.descricao, custoMoedas: v.custoMoedas })
          setPrevia(v.fotoUrl)
        })
        .catch((e) => setErro(lerErro(e)))
    }
  }, [id, editando])

  const alterar = (e) => setDados({ ...dados, [e.target.name]: e.target.value })

  const escolherFoto = (e) => {
    const arquivo = e.target.files[0] ?? null
    setFoto(arquivo)
    if (arquivo) setPrevia(URL.createObjectURL(arquivo))
  }

  const salvar = async (e) => {
    e.preventDefault()
    setSalvando(true)
    setErro({ mensagem: null, campos: {} })
    const corpo = { ...dados, custoMoedas: dados.custoMoedas === '' ? null : Number(dados.custoMoedas) }
    try {
      if (editando) await vantagensApi.atualizar(id, corpo, foto)
      else await vantagensApi.cadastrar(sessao.id, corpo, foto)
      navegar('/empresa')
    } catch (err) {
      setErro(lerErro(err))
    } finally {
      setSalvando(false)
    }
  }

  const c = erro.campos
  return (
    <>
      <header className="cabecalho-pagina">
        <h1>{editando ? 'Editar vantagem' : 'Nova vantagem'}</h1>
        <p className="suave">Produto ou desconto que os alunos podem trocar por moedas.</p>
      </header>
      {erro.mensagem && <p className="alerta">{erro.mensagem}</p>}

      <div className="form-com-previa">
        <form onSubmit={salvar} noValidate>
          <fieldset>
            <div className="grade">
              <Campo rotulo="Nome" nome="nome" value={dados.nome} onChange={alterar} erro={c.nome} largura="l3" placeholder="Ex.: 20% de desconto no almoço" />
              <Campo rotulo="Custo em moedas" nome="custoMoedas" type="number" min="1" value={dados.custoMoedas} onChange={alterar} erro={c.custoMoedas} />
              <Campo rotulo="Descrição" erro={c.descricao} largura="l4">
                <textarea name="descricao" rows={4} maxLength={500} value={dados.descricao} onChange={alterar} placeholder="Explique o que o aluno recebe e como usar" />
              </Campo>
              <Campo rotulo={editando ? 'Trocar foto (opcional)' : 'Foto do produto'} largura="l4">
                <label className="upload">
                  <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={escolherFoto} />
                  <span>{foto ? foto.name : 'Escolher imagem (JPG, PNG, WEBP ou GIF, até 5 MB)'}</span>
                </label>
              </Campo>
            </div>
          </fieldset>
          <div className="rodape-form">
            <Link to="/empresa" className="botao claro">Cancelar</Link>
            <button type="submit" className="botao primario" disabled={salvando}>{salvando ? 'Salvando…' : 'Salvar vantagem'}</button>
          </div>
        </form>

        <aside>
          <span className="sobretitulo">Como o aluno verá</span>
          <article className="produto">
            <div className="produto-foto">
              {previa ? <img src={previa} alt="Prévia" /> : <div className="foto-vazia">Sem foto</div>}
            </div>
            <div className="produto-corpo">
              <span className="produto-empresa">{sessao.nome}</span>
              <h3>{dados.nome || 'Nome da vantagem'}</h3>
              <p>{dados.descricao || 'Descrição da vantagem'}</p>
            </div>
            <div className="produto-rodape">
              <Valor valor={Number(dados.custoMoedas) || 0} className="preco" />
            </div>
          </article>
        </aside>
      </div>
    </>
  )
}
