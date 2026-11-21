-- Lab04S02 (implantacao na nuvem): as fotos das vantagens saem do disco e passam a ficar no banco.
-- No Render gratuito o disco do servidor e apagado a cada reinicio ou deploy; o banco e o unico
-- lugar que persiste. O nome continua sendo o identificador publico usado em /api/fotos/{nome}.
CREATE TABLE foto (
    nome      VARCHAR(60) PRIMARY KEY,
    dados     BYTEA       NOT NULL,
    criada_em TIMESTAMP   NOT NULL
);
