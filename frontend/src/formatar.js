const numero = new Intl.NumberFormat('pt-BR')

export const moedas = (n) => numero.format(n ?? 0)

export const dataHora = (iso) =>
  new Date(iso).toLocaleString('pt-BR', { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' })

export const data = (iso) => new Date(iso).toLocaleDateString('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' })

export const iniciais = (nome) =>
  nome
    .split(/\s+/)
    .filter((p) => p.length > 2 || /^[A-Z]/.test(p))
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')

/** 1o de fevereiro ou 1o de agosto, o que vier primeiro (RN04). */
export function proximoCredito(hoje = new Date()) {
  const ano = hoje.getFullYear()
  const candidatos = [new Date(ano, 1, 1), new Date(ano, 7, 1), new Date(ano + 1, 1, 1)]
  return candidatos.find((d) => d > hoje)
}
