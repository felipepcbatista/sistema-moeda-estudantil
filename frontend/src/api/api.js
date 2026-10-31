import axios from 'axios'

// Cliente HTTP (componente "Cliente HTTP (Axios)" do diagrama de componentes).
const http = axios.create({ baseURL: '/api' })

// Token JWT da sessao: enviado em toda requisicao; resposta 401 (token ausente, invalido ou expirado)
// avisa a sessao para sair e voltar ao login.
let token = null
let aoExpirar = () => {}

export function definirToken(novo) {
  token = novo
}

export function quandoSessaoExpirar(callback) {
  aoExpirar = callback
}

http.interceptors.request.use((config) => {
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    if (erro.response?.status === 401 && !erro.config.url.endsWith('/auth/login')) aoExpirar()
    return Promise.reject(erro)
  },
)

export const instituicoesApi = {
  listar: () => http.get('/instituicoes').then((r) => r.data),
}

export const alunosApi = {
  listar: () => http.get('/alunos').then((r) => r.data),
  buscar: (id) => http.get(`/alunos/${id}`).then((r) => r.data),
  cadastrar: (dados) => http.post('/alunos', dados).then((r) => r.data),
  atualizar: (id, dados) => http.put(`/alunos/${id}`, dados).then((r) => r.data),
  excluir: (id) => http.delete(`/alunos/${id}`),
}

export const empresasApi = {
  listar: () => http.get('/empresas').then((r) => r.data),
  buscar: (id) => http.get(`/empresas/${id}`).then((r) => r.data),
  cadastrar: (dados) => http.post('/empresas', dados).then((r) => r.data),
  atualizar: (id, dados) => http.put(`/empresas/${id}`, dados).then((r) => r.data),
  excluir: (id) => http.delete(`/empresas/${id}`),
  resgates: (id) => http.get(`/empresas/${id}/resgates`).then((r) => r.data),
}

export const authApi = {
  login: (login, senha) => http.post('/auth/login', { login, senha }).then((r) => r.data),
}

export const professoresApi = {
  buscar: (id) => http.get(`/professores/${id}`).then((r) => r.data),
  alunos: (id) => http.get(`/professores/${id}/alunos`).then((r) => r.data),
  enviar: (id, dados) => http.post(`/professores/${id}/envios`, dados).then((r) => r.data),
  extrato: (id) => http.get(`/professores/${id}/extrato`).then((r) => r.data),
}

export const extratoAlunoApi = (id) => http.get(`/alunos/${id}/extrato`).then((r) => r.data)

export const resgatarApi = (alunoId, vantagemId) =>
  http.post(`/alunos/${alunoId}/resgates`, { vantagemId }).then((r) => r.data)

/** Vantagem vai como multipart: parte "dados" (JSON) + parte "foto" (arquivo, opcional na edicao). */
function multipartVantagem(dados, foto) {
  const form = new FormData()
  form.append('dados', new Blob([JSON.stringify(dados)], { type: 'application/json' }))
  if (foto) form.append('foto', foto)
  return form
}

export const vantagensApi = {
  ativas: () => http.get('/vantagens').then((r) => r.data),
  buscar: (id) => http.get(`/vantagens/${id}`).then((r) => r.data),
  daEmpresa: (empresaId) => http.get(`/empresas/${empresaId}/vantagens`).then((r) => r.data),
  cadastrar: (empresaId, dados, foto) =>
    http.post(`/empresas/${empresaId}/vantagens`, multipartVantagem(dados, foto)).then((r) => r.data),
  atualizar: (id, dados, foto) => http.put(`/vantagens/${id}`, multipartVantagem(dados, foto)).then((r) => r.data),
  desativar: (id) => http.post(`/vantagens/${id}/desativar`).then((r) => r.data),
  reativar: (id) => http.post(`/vantagens/${id}/reativar`).then((r) => r.data),
}

/** Converte o ErroResponse da API em { mensagem, campos } para a tela. */
export function lerErro(erro) {
  const corpo = erro.response?.data
  if (corpo?.mensagem) {
    return { mensagem: corpo.mensagem, campos: corpo.campos ?? {} }
  }
  return { mensagem: 'Não foi possível falar com o servidor. O back-end está rodando?', campos: {} }
}

export const formatarCpf = (cpf) => cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')

export const formatarCnpj = (cnpj) =>
  cnpj.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
