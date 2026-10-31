/** Icone da moeda estudantil (capelo sobre uma moeda dourada). */
export default function Moeda({ tamanho = 20, className = '' }) {
  return (
    <svg className={`moeda ${className}`} width={tamanho} height={tamanho} viewBox="0 0 32 32" aria-hidden="true">
      <circle cx="16" cy="16" r="15" fill="#F5B83D" />
      <circle cx="16" cy="16" r="11.5" fill="none" stroke="#C98A0B" strokeWidth="1.6" />
      <path d="M16 9.5 7.5 13.6 16 17.7l8.5-4.1L16 9.5Z" fill="#7A4E00" />
      <path d="M11 15.6v3.3c0 1.3 2.2 2.6 5 2.6s5-1.3 5-2.6v-3.3L16 18l-5-2.4Z" fill="#7A4E00" />
      <path d="M23.4 14.2v4.6" stroke="#7A4E00" strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  )
}

/** Valor em moedas com o icone, usado em saldos e precos. */
export function Valor({ valor, tamanho = 16, className = '' }) {
  return (
    <span className={`valor ${className}`}>
      <Moeda tamanho={tamanho} />
      {new Intl.NumberFormat('pt-BR').format(valor ?? 0)}
    </span>
  )
}
