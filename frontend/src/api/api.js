import axios from 'axios'

// Cliente HTTP (componente "Cliente HTTP (Axios)" do diagrama de componentes).
const http = axios.create({ baseURL: '/api' })

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
