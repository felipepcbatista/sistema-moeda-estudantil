-- Carga inicial (RN02, RN03): instituicoes e professores chegam pre-cadastrados,
-- pois o enunciado nao define ator que os cadastre (a lista vem da instituicao na parceria).
-- Cada professor ja recebe o credito do semestre 2026-2 (RN04), registrado como transacao.
-- Senha de todos os professores de exemplo: prof123 (hash BCrypt).

INSERT INTO instituicao_ensino (nome) VALUES ('PUC Minas');
INSERT INTO instituicao_ensino (nome) VALUES ('UFMG');
INSERT INTO instituicao_ensino (nome) VALUES ('CEFET-MG');

-- Professor 1
INSERT INTO conta (saldo) VALUES (1000);
INSERT INTO usuario (login, senha_hash)
VALUES ('prof.ana', '$2a$10$WmPmwHLG329vvIF269fezuX3WuDU2shxO266DSrZ2uMiV3L18SgKO');
INSERT INTO professor (id, nome, cpf, departamento, instituicao_id, conta_id)
VALUES ((SELECT id FROM usuario WHERE login = 'prof.ana'), 'Ana Souza', '52998224725', 'Engenharia de Software',
        (SELECT id FROM instituicao_ensino WHERE nome = 'PUC Minas'), (SELECT MAX(id) FROM conta));
INSERT INTO transacao (tipo, valor, data_hora, conta_destino_id, semestre)
VALUES ('CREDITO', 1000, TIMESTAMP '2026-08-01 00:00:00', (SELECT MAX(id) FROM conta), '2026-2');

-- Professor 2
INSERT INTO conta (saldo) VALUES (1000);
INSERT INTO usuario (login, senha_hash)
VALUES ('prof.bruno', '$2a$10$WmPmwHLG329vvIF269fezuX3WuDU2shxO266DSrZ2uMiV3L18SgKO');
INSERT INTO professor (id, nome, cpf, departamento, instituicao_id, conta_id)
VALUES ((SELECT id FROM usuario WHERE login = 'prof.bruno'), 'Bruno Lima', '11144477735', 'Ciência da Computação',
        (SELECT id FROM instituicao_ensino WHERE nome = 'PUC Minas'), (SELECT MAX(id) FROM conta));
INSERT INTO transacao (tipo, valor, data_hora, conta_destino_id, semestre)
VALUES ('CREDITO', 1000, TIMESTAMP '2026-08-01 00:00:00', (SELECT MAX(id) FROM conta), '2026-2');

-- Professor 3
INSERT INTO conta (saldo) VALUES (1000);
INSERT INTO usuario (login, senha_hash)
VALUES ('prof.carla', '$2a$10$WmPmwHLG329vvIF269fezuX3WuDU2shxO266DSrZ2uMiV3L18SgKO');
INSERT INTO professor (id, nome, cpf, departamento, instituicao_id, conta_id)
VALUES ((SELECT id FROM usuario WHERE login = 'prof.carla'), 'Carla Mendes', '39053344705', 'Sistemas de Informação',
        (SELECT id FROM instituicao_ensino WHERE nome = 'UFMG'), (SELECT MAX(id) FROM conta));
INSERT INTO transacao (tipo, valor, data_hora, conta_destino_id, semestre)
VALUES ('CREDITO', 1000, TIMESTAMP '2026-08-01 00:00:00', (SELECT MAX(id) FROM conta), '2026-2');
