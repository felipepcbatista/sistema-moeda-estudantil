# Apresentação final — Lab03S03

Apresentação da arquitetura do sistema, da camada de persistência e tutorial das tecnologias (20 min), conforme o enunciado do Laboratório 3.

**Slides:** https://claude.ai/artifact/5McHAieW5BrBgzzpKYjUZm (18 slides, com notas de fala em cada um; exporta para PDF ou PPTX pelo menu do deck).

A pasta [`fonte-slides/`](fonte-slides/) guarda o código-fonte de cada slide, versionado junto com o projeto.

## Roteiro e tempo

| # | Slide | Bloco | Tempo |
|---|---|---|---|
| 1 | Capa | Abertura | 0:30 |
| 2 | Mérito vira moeda, moeda vira vantagem | O sistema | 1:00 |
| 3 | Três sprints, um sistema completo | O sistema | 1:00 |
| 4 | MVC em três camadas e um banco | Arquitetura | 1:00 |
| 5 | Diagrama de componentes | Arquitetura | 1:30 |
| 6 | Caminho de “enviar 50 moedas” | Arquitetura | 1:30 |
| 7 | Toda moeda tem origem, destino e motivo | Persistência | 0:30 |
| 8 | Modelo ER | Persistência | 1:00 |
| 9 | Das classes para as tabelas (JOINED, SINGLE_TABLE) | Persistência | 1:00 |
| 10 | ORM + Repository + Flyway | Persistência | 1:00 |
| 11 | Saldo nunca fica errado (transação, travas, CHECK) | Persistência | 1:00 |
| 12 | O que cada escolha custou | Decisões | 1:00 |
| 13 | A stack, camada por camada | Tutorial | 0:45 |
| 14 | Um CRUD em quatro peças | Tutorial | 1:00 |
| 15 | Cada um só mexe no que é seu (JWT) | Tutorial | 1:00 |
| 16 | Do clone ao sistema no ar | Tutorial | 0:45 |
| 17 | Demonstração ao vivo | Demo | 5:00 |
| 18 | Próximos passos e perguntas | Fechamento | 0:30 |

Total: cerca de 20 minutos.

## Antes da apresentação

1. `docker compose up -d`, `mvn spring-boot:run` (em `backend/`), `npm run dev` (em `frontend/`).
2. Banco limpo + `node scripts/popular-demo.mjs`, para os dados da demonstração.
3. Abrir em abas: http://localhost:5180 (sistema) e http://localhost:8025 (emails).
4. Plano B, se a demonstração falhar: `mvn test` em `backend/` (28 testes).

## A preencher pelo grupo

- Slide 1: nomes dos integrantes.
- Slide 18: dono e prazo de cada próximo passo.
