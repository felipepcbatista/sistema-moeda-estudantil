/** Input com rotulo e mensagem de erro vinda da validacao do back-end. */
export default function Campo({ rotulo, nome, erro, largura, children, ...props }) {
  return (
    <label className={`campo ${largura ?? ''}`}>
      <span>{rotulo}</span>
      {children ?? <input name={nome} {...props} aria-invalid={Boolean(erro)} />}
      {erro && <small className="erro-campo">{erro}</small>}
    </label>
  )
}
