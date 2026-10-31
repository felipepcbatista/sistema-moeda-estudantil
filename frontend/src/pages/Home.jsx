import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { vantagensApi } from '../api/api.js'
import Moeda, { Valor } from '../components/Moeda.jsx'

const PASSOS = [
  ['Professor recebe moedas', 'A cada semestre, 1.000 moedas por professor. O que não for usado acumula.'],
  ['Reconhece o aluno', 'Envia moedas com o motivo: participação, bom comportamento, um trabalho excelente.'],
  ['Aluno escolhe a vantagem', 'Descontos e produtos de empresas parceiras, com preço em moedas.'],
  ['Troca com código', 'Aluno e empresa recebem o mesmo código por email, conferido na hora da troca.'],
]

export default function Home() {
  const [vantagens, setVantagens] = useState([])

  useEffect(() => {
    vantagensApi.ativas().then((v) => setVantagens(v.slice(0, 4))).catch(() => {})
  }, [])

  return (
    <>
      <section className="hero">
        <div className="hero-texto">
          <span className="sobretitulo">Reconhecimento do mérito estudantil</span>
          <h1>Bom desempenho vira <em>moeda</em>. Moeda vira vantagem.</h1>
          <p>
            Professores reconhecem seus alunos com moedas virtuais. Alunos trocam as moedas por descontos e produtos
            de empresas parceiras.
          </p>
          <div className="hero-acoes">
            <Link to="/entrar" className="botao ouro grande">Entrar</Link>
            <Link to="/cadastro/aluno" className="botao claro grande">Sou aluno, quero me cadastrar</Link>
          </div>
          <p className="hero-nota">
            Representa uma empresa? <Link to="/cadastro/empresa">Ofereça vantagens aos alunos</Link>
          </p>
        </div>

        <div className="hero-visual" aria-hidden="true">
          <div className="cartao-flutuante cartao-saldo">
            <span>Saldo de Maria</span>
            <strong><Moeda tamanho={30} /> 250</strong>
          </div>
          <div className="cartao-flutuante cartao-reconhecimento">
            <div className="linha-autor"><span className="bolinha">AS</span> Prof. Ana Souza enviou <b>+50</b></div>
            <p>“Apresentação excelente do trabalho final. Parabéns pela pesquisa!”</p>
          </div>
          <div className="cartao-flutuante cartao-cupom">
            <span>Cupom</span>
            <code>K7Q2MXRA</code>
          </div>
        </div>
      </section>

      <section className="secao">
        <h2 className="titulo-secao">Como funciona</h2>
        <ol className="passos">
          {PASSOS.map(([titulo, texto], i) => (
            <li key={titulo}>
              <span className="passo-num">{i + 1}</span>
              <h3>{titulo}</h3>
              <p>{texto}</p>
            </li>
          ))}
        </ol>
      </section>

      {vantagens.length > 0 && (
        <section className="secao">
          <div className="cabecalho-secao">
            <h2 className="titulo-secao">Vantagens disponíveis</h2>
            <Link to="/cadastro/aluno">Cadastre-se para resgatar →</Link>
          </div>
          <div className="vitrine">
            {vantagens.map((v) => (
              <article key={v.id} className="produto">
                <div className="produto-foto"><img src={v.fotoUrl} alt={v.nome} loading="lazy" /></div>
                <div className="produto-corpo">
                  <span className="produto-empresa">{v.empresaNome}</span>
                  <h3>{v.nome}</h3>
                </div>
                <div className="produto-rodape"><Valor valor={v.custoMoedas} className="preco" /></div>
              </article>
            ))}
          </div>
        </section>
      )}
    </>
  )
}
