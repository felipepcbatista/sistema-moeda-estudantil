import { useEffect } from 'react'

/** Janela sobre a tela; fecha com Esc ou clicando fora. */
export default function Modal({ titulo, aoFechar, children, largura = 480 }) {
  useEffect(() => {
    const tecla = (e) => e.key === 'Escape' && aoFechar()
    window.addEventListener('keydown', tecla)
    return () => window.removeEventListener('keydown', tecla)
  }, [aoFechar])

  return (
    <div className="modal-fundo" onMouseDown={(e) => e.target === e.currentTarget && aoFechar()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo} style={{ maxWidth: largura }}>
        <div className="modal-topo">
          <h2>{titulo}</h2>
          <button type="button" className="fechar" onClick={aoFechar} aria-label="Fechar">×</button>
        </div>
        {children}
      </div>
    </div>
  )
}
