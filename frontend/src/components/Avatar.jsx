import { iniciais } from '../formatar.js'

const CORES = ['#2F5BD3', '#0F8A6A', '#B5562E', '#7A3FC4', '#B23A69', '#2C7A9E']

/** Iniciais do nome num circulo com cor estavel por pessoa. */
export default function Avatar({ nome, tamanho = 40 }) {
  const cor = CORES[[...nome].reduce((s, c) => s + c.charCodeAt(0), 0) % CORES.length]
  return (
    <span className="avatar" style={{ width: tamanho, height: tamanho, background: cor, fontSize: tamanho * 0.38 }}>
      {iniciais(nome)}
    </span>
  )
}
