// Popula o sistema com dados de demonstracao usando a propria API (passa pelas mesmas regras de negocio).
// Uso, com o back-end rodando e o banco recem-criado:  node scripts/popular-demo.mjs
// Cada acao usa o token JWT do proprio ator (empresa cadastra a vantagem, professor envia, aluno resgata).
// Senhas: alunos = aluno123, empresas = empresa123, professores (carga inicial) = prof123
import { readFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const API = process.env.API_URL ?? 'http://localhost:8080/api'
const FOTOS = join(dirname(fileURLToPath(import.meta.url)), 'demo-fotos')

async function chamar(metodo, rota, corpo, token) {
  const opcoes = { method: metodo, headers: {} }
  if (token) opcoes.headers.Authorization = `Bearer ${token}`
  if (corpo instanceof FormData) opcoes.body = corpo
  else if (corpo) {
    opcoes.headers['Content-Type'] = 'application/json'
    opcoes.body = JSON.stringify(corpo)
  }
  const r = await fetch(API + rota, opcoes)
  const json = r.status === 204 ? null : await r.json()
  if (!r.ok) throw new Error(`${metodo} ${rota} -> ${r.status}: ${JSON.stringify(json)}`)
  return json
}

// CPF e CNPJ validos (digitos verificadores calculados), para passar na validacao da API
function digito(base, pesos) {
  const resto = base.reduce((s, d, i) => s + d * pesos[i], 0) % 11
  return resto < 2 ? 0 : 11 - resto
}
function cpf(semente) {
  const b = String(semente).padStart(9, '0').split('').map(Number)
  b.push(digito(b, [10, 9, 8, 7, 6, 5, 4, 3, 2]))
  b.push(digito(b, [11, 10, 9, 8, 7, 6, 5, 4, 3, 2]))
  return b.join('')
}
function cnpj(semente) {
  const b = (String(semente).padStart(8, '0') + '0001').split('').map(Number)
  b.push(digito(b, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]))
  b.push(digito(b, [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]))
  return b.join('')
}

const ALUNOS = [
  ['maria.silva', 'Maria Silva', 'Engenharia de Software', 'PUC Minas', 'Rua Dom José Gaspar', '500', 'Coração Eucarístico'],
  ['joao.pereira', 'João Pereira', 'Ciência da Computação', 'PUC Minas', 'Rua Padre Pedro Pinto', '1210', 'Venda Nova'],
  ['beatriz.costa', 'Beatriz Costa', 'Engenharia de Software', 'PUC Minas', 'Avenida Amazonas', '3200', 'Gameleira'],
  ['lucas.almeida', 'Lucas Almeida', 'Sistemas de Informação', 'PUC Minas', 'Rua Itapecerica', '85', 'Lagoinha'],
  ['pedro.santos', 'Pedro Santos', 'Ciência da Computação', 'UFMG', 'Avenida Antônio Carlos', '6627', 'Pampulha'],
]

const EMPRESAS = [
  ['cantina.coracao', 'Cantina Coração', [
    ['Almoço completo no RU', 'Prato do dia, suco e sobremesa no restaurante universitário.', 80, 'almoco'],
    ['Café + pão de queijo', 'Um café coado e um pão de queijo quentinho para o intervalo.', 25, 'cafe'],
  ]],
  ['livraria.paginaviva', 'Livraria Página Viva', [
    ['Vale-livro de R$ 30', 'Crédito de R$ 30 em qualquer livro da loja do campus.', 150, 'livro'],
    ['Kit cadernos', 'Dois cadernos universitários de 10 matérias e um estojo.', 60, 'cadernos'],
  ]],
  ['papelaria.pontovirgula', 'Papelaria Ponto & Vírgula', [
    ['100 impressões grátis', 'Impressões em preto e branco para trabalhos e listas de exercícios.', 40, 'impressoes'],
  ]],
  ['horizonte.idiomas', 'Horizonte Idiomas', [
    ['15% de desconto no curso', 'Desconto de 15% na matrícula de qualquer curso de idiomas, mais aula experimental.', 200, 'idiomas'],
  ]],
  ['academia.movimento', 'Academia Movimento', [
    ['Um mês de academia', 'Acesso livre à musculação e às aulas coletivas por 30 dias.', 300, 'academia'],
  ]],
]

// [professor, aluno, moedas, motivo]
const token = async (login, senha) => (await chamar('POST', '/auth/login', { login, senha })).token

const ENVIOS = [
  ['prof.ana', 'maria.silva', 120, 'Apresentação excelente do trabalho final de Projeto de Software. Pesquisa muito bem fundamentada!'],
  ['prof.ana', 'joao.pereira', 80, 'Ajudou os colegas na monitoria de algoritmos durante toda a semana de provas.'],
  ['prof.ana', 'beatriz.costa', 50, 'Participação ativa em todas as aulas e ótimas perguntas sobre arquitetura.'],
  ['prof.bruno', 'maria.silva', 60, 'Melhor nota da turma na prova de Estruturas de Dados.'],
  ['prof.bruno', 'lucas.almeida', 100, 'Projeto de extensão com impacto real na comunidade do bairro.'],
  ['prof.bruno', 'joao.pereira', 40, 'Entregou todos os exercícios antes do prazo, com código limpo e testado.'],
  ['prof.carla', 'pedro.santos', 90, 'Liderou a equipe na maratona de programação e ficou em 2º lugar.'],
]

// [aluno, vantagem]
const RESGATES = [
  ['maria.silva', 'Café + pão de queijo'],
  ['joao.pereira', '100 impressões grátis'],
]

async function main() {
  try {
    await chamar('POST', '/auth/login', { login: 'maria.silva', senha: 'aluno123' })
    console.log('Os dados de demonstração já existem. Para recomeçar: docker compose down -v && docker compose up -d, e reinicie o back-end.')
    return
  } catch {
    // ainda nao populado
  }

  const instituicoes = Object.fromEntries((await chamar('GET', '/instituicoes')).map((i) => [i.nome, i.id]))
  const ids = {}

  for (const [i, [login, nome, curso, inst, rua, numero, bairro]] of ALUNOS.entries()) {
    const a = await chamar('POST', '/alunos', {
      nome, curso, login, senha: 'aluno123',
      email: `${login}@aluno.exemplo.com`,
      cpf: cpf(123456780 + i * 7919),
      rg: `MG-${14_000_000 + i * 1_234_567}`,
      instituicaoId: instituicoes[inst],
      endereco: { logradouro: rua, numero, complemento: null, bairro, cidade: 'Belo Horizonte', uf: 'MG', cep: '30535901' },
    })
    ids[login] = a.id
    console.log('aluno', nome)
  }

  const vantagens = {}
  for (const [i, [login, nomeFantasia, ofertas]] of EMPRESAS.entries()) {
    const e = await chamar('POST', '/empresas', {
      nomeFantasia, login, senha: 'empresa123',
      cnpj: cnpj(11222333 + i * 104729),
      email: `contato@${login.replace('.', '')}.exemplo.com`,
    })
    const tokenEmpresa = await token(login, 'empresa123')
    for (const [nome, descricao, custoMoedas, foto] of ofertas) {
      const form = new FormData()
      form.append('dados', new Blob([JSON.stringify({ nome, descricao, custoMoedas })], { type: 'application/json' }))
      form.append('foto', new Blob([readFileSync(join(FOTOS, `${foto}.png`))], { type: 'image/png' }), `${foto}.png`)
      vantagens[nome] = (await chamar('POST', `/empresas/${e.id}/vantagens`, form, tokenEmpresa)).id
    }
    console.log('empresa', nomeFantasia, `(${ofertas.length} vantagem(ns))`)
  }

  for (const [prof, aluno, valor, motivo] of ENVIOS) {
    const p = await chamar('POST', '/auth/login', { login: prof, senha: 'prof123' })
    await chamar('POST', `/professores/${p.id}/envios`, { alunoId: ids[aluno], valor, motivo }, p.token)
    console.log(`envio ${prof} -> ${aluno}: ${valor}`)
  }

  for (const [aluno, vantagem] of RESGATES) {
    const r = await chamar('POST', `/alunos/${ids[aluno]}/resgates`, { vantagemId: vantagens[vantagem] }, await token(aluno, 'aluno123'))
    console.log(`resgate ${aluno}: ${vantagem} (código ${r.codigo})`)
  }

  console.log('\nPronto. Contas: maria.silva / aluno123 · prof.ana / prof123 · cantina.coracao / empresa123')
}

main().catch((e) => {
  console.error(e.message)
  process.exit(1)
})
