# Sistema de Moeda Estudantil — PUC Minas | Projeto de Software (2S2026)

| Sprint | Entrega | Seções |
|---|---|---|
| **Lab03S01** | Modelagem: casos de uso, histórias de usuário, diagrama de classes e de componentes | 1–7 |
| **Lab03S02** | Modelo ER, estratégia de acesso a dados (ORM + Repository/DAO), CRUDs de aluno e empresa parceira (front-end + back-end) | 8–11 |

## 1. Contexto

Sistema para estimular o reconhecimento do mérito estudantil por meio de uma moeda virtual.
Professores distribuem moedas aos seus alunos; alunos trocam as moedas por vantagens
(produtos e descontos) oferecidas por empresas parceiras.

## 2. Atores

| Ator | Papel |
|---|---|
| **Aluno** | Se cadastra no sistema, consulta extrato, consulta e resgata vantagens com suas moedas. |
| **Professor** | Pré-cadastrado pela instituição. Envia moedas a alunos e consulta o próprio extrato. |
| **Empresa Parceira** | Se cadastra no sistema e cadastra as vantagens que oferece, com custo em moedas. |

Todos os atores autenticam-se com login e senha antes de qualquer operação (exceto o próprio cadastro).

## 3. Regras de negócio extraídas do enunciado

- **RN01** — Cadastro de aluno exige nome, email, CPF, RG, endereço, instituição de ensino e curso.
- **RN02** — Instituições de ensino já estão pré-cadastradas; o aluno apenas seleciona uma delas.
- **RN03** — Professores já estão pré-cadastrados (a instituição envia a lista no momento da parceria), com nome, CPF, departamento e vínculo explícito a uma instituição.
- **RN04** — A cada semestre, cada professor recebe 1.000 moedas. O saldo é acumulável: moedas não distribuídas permanecem e as 1.000 novas são somadas ao saldo corrente. **É um processo de sistema disparado por data (início de semestre), sem ator humano acionando — por isso não aparece como caso de uso no diagrama.**
- **RN05** — Para enviar moedas, o professor precisa ter saldo suficiente, indicar o aluno e informar o motivo do reconhecimento (mensagem aberta, obrigatória).
- **RN06** — Ao receber moedas, o aluno é notificado por email.
- **RN07** — Extrato: o professor vê saldo e envios realizados; o aluno vê saldo, recebimentos e trocas.
- **RN08** — Vantagem cadastrada pela empresa parceira exige nome, descrição, foto do produto e custo em moedas.
- **RN09** — Ao resgatar uma vantagem, o custo é descontado do saldo do aluno (que precisa ter saldo suficiente).
- **RN10** — No resgate, o sistema gera um código único e envia dois emails com esse código: cupom para o aluno (uso na troca presencial) e aviso para a empresa parceira (conferência da troca).
- **RN11** — O **serviço de email** é um sistema externo passivo, apenas acionado pelo sistema. **Não é modelado como ator** no diagrama de casos de uso (não interage buscando um objetivo próprio); aparece no diagrama de componentes como integração (Servidor SMTP).

## 4. Histórias de Usuário

### US01 — Autenticação
**Como** usuário do sistema (Aluno, Professor ou Empresa Parceira),
**quero** informar login e senha,
**para** acessar as funcionalidades correspondentes ao meu perfil.

*Critérios de aceite:*
- Credenciais inválidas impedem o acesso e exibem mensagem de erro.
- Cada perfil só acessa as funcionalidades do próprio papel.
- É pré-condição para todas as demais histórias, exceto US02 e US04 (cadastro).

---

### US02 — Cadastrar-se como aluno
**Como** aluno,
**quero** me cadastrar informando nome, email, CPF, RG, endereço, instituição de ensino e curso,
**para** participar do sistema de mérito.

*Critérios de aceite:*
- Todos os campos da RN01 são obrigatórios.
- A instituição é selecionada de uma lista de instituições pré-cadastradas (RN02).
- CPF e email são únicos no sistema.
- O cadastro define login e senha de acesso.
- O aluno inicia com saldo de 0 moedas.

---

### US03 — Manter cadastro de aluno
**Como** aluno,
**quero** consultar, editar e excluir meu cadastro,
**para** manter meus dados atualizados.

*Critérios de aceite:*
- Edição respeita as mesmas validações do cadastro (US02).
- Um aluno só acessa e altera o próprio cadastro.

---

### US04 — Cadastrar-se como empresa parceira
**Como** empresa parceira,
**quero** me cadastrar no sistema,
**para** oferecer vantagens aos alunos.

*Critérios de aceite:*
- Exige nome, CNPJ e email; CNPJ e email são únicos.
- O cadastro define login e senha de acesso.

---

### US05 — Manter cadastro de empresa parceira
**Como** empresa parceira,
**quero** consultar, editar e excluir meu cadastro,
**para** manter meus dados atualizados.

*Critérios de aceite:*
- Uma empresa só acessa e altera o próprio cadastro.

---

### US06 — Gerenciar vantagens
**Como** empresa parceira,
**quero** cadastrar, editar e desativar as vantagens que ofereço,
**para** que os alunos possam trocá-las por moedas.

*Critérios de aceite:*
- Cadastro exige nome, descrição, foto do produto e custo em moedas (RN08).
- Custo deve ser maior que zero.
- Vantagem desativada deixa de aparecer para os alunos, mas resgates anteriores são preservados no histórico.

---

### US07 — Enviar moedas
**Como** professor,
**quero** enviar moedas a um aluno informando o motivo do reconhecimento,
**para** valorizar bom comportamento, participação em aula etc.

*Critérios de aceite:*
- Bloqueia o envio se o valor for maior que o saldo do professor (RN05).
- O motivo é obrigatório.
- O valor é debitado do professor e creditado ao aluno na mesma operação.
- O aluno recebe email informando valor, professor e motivo (RN06).

---

### US08 — Consultar extrato
**Como** professor ou aluno,
**quero** consultar meu extrato,
**para** ver meu saldo atual e as transações que realizei.

*Critérios de aceite:*
- Exibe saldo atual e transações em ordem cronológica decrescente.
- Professor: créditos semestrais e envios (aluno, valor, motivo, data) (RN07).
- Aluno: recebimentos (professor, valor, motivo, data) e resgates (vantagem, valor, código, data) (RN07).

---

### US09 — Consultar vantagens
**Como** aluno,
**quero** ver as vantagens disponíveis, com descrição, foto, empresa e custo,
**para** escolher em que trocar minhas moedas.

*Critérios de aceite:*
- Lista apenas vantagens ativas.

---

### US10 — Resgatar vantagem
**Como** aluno,
**quero** trocar minhas moedas por uma vantagem,
**para** usufruir do reconhecimento que recebi.

*Critérios de aceite:*
- Bloqueia o resgate se o saldo do aluno for menor que o custo da vantagem (RN09).
- O custo é debitado do saldo do aluno.
- O sistema gera um código único de resgate (RN10).
- O aluno recebe email de cupom com o código; a empresa parceira recebe email com o mesmo código para conferência (RN10).

## 5. Diagrama de Casos de Uso

![Diagrama de Casos de Uso](docs/diagramas/diagrama-caso-de-uso.png)

Fonte PlantUML em [`docs/diagramas/diagrama-caso-de-uso.puml`](docs/diagramas/diagrama-caso-de-uso.puml).

**Notas de escopo:**
- **Crédito semestral de 1.000 moedas** (RN04) não é caso de uso: é processo de sistema disparado por data. No código, vira um job agendado (ver Diagrama de Componentes).
- **Pré-cadastro de instituições e professores** (RN02, RN03) não é caso de uso: o enunciado não define um ator que faça esse cadastro (a lista vem da instituição no momento da parceria). É tratado como carga inicial de dados.
- **Serviço de email** não é ator (RN11).
- **Resgatar Vantagem** inclui **Consultar Vantagens**: o aluno precisa selecionar uma vantagem cadastrada para trocar.

## 6. Diagrama de Classes

![Diagrama de Classes](docs/diagramas/diagrama-classes.png)

Fonte PlantUML em [`docs/diagramas/diagrama-classes.puml`](docs/diagramas/diagrama-classes.puml).

**Decisões de modelagem:**
- **`Usuario`** é classe abstrata (login, senha, `autenticar()`) da qual `Aluno`, `Professor` e `EmpresaParceira` herdam — reflete o `<<include>>` de Autenticar-se no diagrama de casos de uso.
- **`Conta`** concentra saldo e as operações `creditar()`/`debitar()`/`possuiSaldo()`. Aluno e Professor possuem uma conta cada (composição); a empresa parceira não tem saldo. Isso evita duplicar a lógica de saldo nas duas classes e centraliza a validação de saldo suficiente (RN05, RN09).
- **`Transacao`** é abstrata, com conta de origem e de destino (ambas opcionais), especializada em:
  - `EnvioMoedas` — professor → aluno, com `motivo` obrigatório;
  - `ResgateVantagem` — aluno → (sem destino), ligada à `Vantagem` e com o `codigo` gerado;
  - `CreditoSemestral` — (sem origem) → professor, com o `semestre`.
  O extrato (US08) é a lista de transações em que a conta aparece como origem ou destino.
- **`InstituicaoEnsino`** é associada a `Professor` (*faz parte de*) — atende ao "é necessário deixar explícito que ele faz parte de uma instituição" — e a `Aluno` (*estuda em*).
- **`Endereco`** é objeto de valor embutido no aluno (`<<embeddable>>`), não entidade própria.
- **`curso`** do aluno é `String`: o enunciado não pede curso como entidade com atributos próprios.
- Pacote **`service`** concentra as regras de negócio (camada Model do MVC, ver Seção 7). `TransacaoService` faz envio, resgate, crédito semestral e extrato numa transação atômica (débito + crédito + registro). **`NotificacaoService`** é interface, com implementação por email — permite trocar ou simular o envio de email em teste.

## 7. Diagrama de Componentes

![Diagrama de Componentes](docs/diagramas/diagrama-componentes.png)

Fonte PlantUML em [`docs/diagramas/diagrama-componentes.puml`](docs/diagramas/diagrama-componentes.puml).

**Arquitetura MVC (exigida pelo enunciado):**

| Camada MVC | Componentes |
|---|---|
| **View** | Front-end React (páginas de Aluno, Professor e Empresa Parceira), consumindo a API via cliente HTTP. |
| **Controller** | Controllers REST do Spring Boot — recebem a requisição, validam a entrada e delegam ao service. |
| **Model** | Entidades JPA (diagrama de classes, pacote `model`) + services com as regras de negócio + repositories de acesso a dados. |

**Tecnologias propostas:**

| Item | Tecnologia |
|---|---|
| Back-end | Java 21 + Spring Boot (Web, Validation, Security, Mail) |
| Persistência | Spring Data JPA (Hibernate) + PostgreSQL |
| Autenticação | Spring Security com JWT |
| Email | JavaMailSender via servidor SMTP |
| Crédito semestral | `@Scheduled` do Spring (job agendado) |
| Front-end | React + Vite, Axios |

A estratégia de acesso a dados (ORM via JPA + Repository) e o modelo ER são detalhados na Sprint 2 (Seções 8 e 9).

## 8. Modelo ER (Lab03S02)

![Modelo ER](docs/diagramas/modelo-er.png)

Fonte PlantUML em [`docs/diagramas/modelo-er.puml`](docs/diagramas/modelo-er.puml). A fonte de verdade do schema é a migração [`V1__schema_inicial.sql`](backend/src/main/resources/db/migration/V1__schema_inicial.sql); o diagrama a espelha coluna a coluna.

**Do diagrama de classes para as tabelas:**

| Decisão | Mapeamento | Por quê |
|---|---|---|
| Herança `Usuario` → `Aluno` / `Professor` / `EmpresaParceira` | **JOINED**: tabela `usuario` (login, senha) + uma tabela por subclasse, com PK = FK para `usuario` | Login único entre todos os perfis numa só constraint; subclasses têm colunas bem diferentes, então tabela única teria muitas colunas nulas. |
| Herança `Transacao` → `EnvioMoedas` / `ResgateVantagem` / `CreditoSemestral` | **SINGLE_TABLE**: tabela `transacao` com discriminador `tipo` | Cada subclasse acrescenta um único campo; o extrato (US08) lê tudo de uma conta numa consulta sem joins. |
| `Endereco` (`<<embeddable>>`) | Colunas na própria tabela `aluno` | Objeto de valor, sem identidade própria. |
| Composição Aluno/Professor ↔ `Conta` | FK `conta_id` única em `aluno` e `professor` | Relação 1:1; a conta é criada e excluída junto com o dono (cascade). |
| Regras de integridade no banco | `CHECK saldo >= 0`, `CHECK custo_moedas > 0`, `CHECK valor > 0`, `UNIQUE` em CPF, CNPJ, email, login e código de resgate | Última barreira caso uma regra escape da camada de serviço. |
| CPF, CNPJ e CEP | Gravados só com dígitos | A API aceita com ou sem máscara e normaliza antes de gravar. |

## 9. Estratégia de acesso a dados (Lab03S02)

**Escolha: ORM (JPA/Hibernate) + padrão Repository/DAO via Spring Data JPA, com schema versionado pelo Flyway.**

![Camada de persistência](docs/diagramas/diagrama-persistencia.png)

Fonte PlantUML em [`docs/diagramas/diagrama-persistencia.puml`](docs/diagramas/diagrama-persistencia.puml).

- **ORM (JPA/Hibernate):** as entidades do pacote `model` são as classes do diagrama de classes, anotadas com o mapeamento para as tabelas. Mantém modelo e código alinhados (critério de avaliação).
- **Repository (DAO):** cada agregado tem uma interface em `repository` que estende `JpaRepository`. O Spring gera a implementação; consultas específicas usam nomes de método (`existsByCpf`) ou JPQL (`findExtrato`). Os services dependem só das interfaces, nunca de SQL.
- **Flyway:** o schema é criado por migrações SQL versionadas (`V1` schema, `V2` carga inicial), e o Hibernate roda com `ddl-auto=validate`, só conferindo se as entidades batem com o banco. O banco evolui por versão, e qualquer divergência entre modelo e schema derruba a aplicação na subida, em vez de virar erro silencioso.
- **Carga inicial (V2):** instituições (PUC Minas, UFMG, CEFET-MG) e três professores, cada um já com o crédito de 1.000 moedas do semestre 2026-2 registrado como `CreditoSemestral` (RN02, RN03, RN04).
- **Senhas:** só o hash BCrypt é gravado (`usuario.senha_hash`).

**Alternativas consideradas:** JDBC puro com DAOs escritos à mão (mais código repetitivo e o mapeamento objeto-relacional feito manualmente, sem ganho para este domínio) e `ddl-auto=update` no lugar do Flyway (o schema ficaria implícito e não versionado).

**Mudanças no diagrama de classes nesta sprint:** os métodos `cadastrar`/`atualizar` de `AlunoService` e `EmpresaParceiraService` passaram a receber DTOs de requisição (`AlunoRequest`, `EmpresaParceiraRequest`) em vez da entidade, porque a requisição traz dados que não pertencem à entidade (senha em texto puro, id da instituição). Entrou também o `InstituicaoEnsinoService`, que alimenta a seleção de instituição no cadastro de aluno (RN02).

## 10. CRUDs de aluno e empresa parceira (Lab03S02)

**Fluxo MVC:** tela React → Axios → Controller REST → Service (regras) → Repository → PostgreSQL.

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/instituicoes` | Instituições pré-cadastradas |
| GET | `/api/alunos` | Lista alunos (com instituição e saldo) |
| GET | `/api/alunos/{id}` | Consulta aluno |
| POST | `/api/alunos` | Cadastra aluno (US02), saldo inicial 0 |
| PUT | `/api/alunos/{id}` | Edita aluno (US03); senha em branco mantém a atual |
| DELETE | `/api/alunos/{id}` | Exclui aluno; bloqueado se houver movimentação de moedas |
| GET | `/api/empresas` | Lista empresas parceiras |
| GET | `/api/empresas/{id}` | Consulta empresa |
| POST | `/api/empresas` | Cadastra empresa (US04) |
| PUT | `/api/empresas/{id}` | Edita empresa (US05) |
| DELETE | `/api/empresas/{id}` | Exclui empresa e suas vantagens; bloqueado se alguma vantagem já foi resgatada |

**Respostas de erro:** `400` com erro por campo (validação), `404` (não encontrado), `422` (regra de negócio: CPF/CNPJ/email/login duplicado, senha ausente no cadastro, exclusão bloqueada). As telas exibem a mensagem geral e o erro embaixo de cada campo.

**Telas:** listagem e formulário de aluno (dados pessoais, vínculo acadêmico com seleção da instituição, endereço, acesso) e de empresa parceira.

**Testes automatizados** (`backend/src/test`): 13 testes, com testes de integração MockMvc rodando as mesmas migrações Flyway num H2 em modo PostgreSQL, cobrindo cadastro, normalização, validação, duplicidade (inclusive login de professor), edição sem troca de senha, exclusão e os dois bloqueios de exclusão, além de testes unitários de `Conta`.

**Fica para a Sprint 3:** autenticação (Spring Security + JWT) e restrição de cada perfil ao próprio cadastro, versão final dos CRUDs e slides da arquitetura e da camada de persistência.

## 11. Como executar

Pré-requisitos: Java 21, Maven, Node 20+ e Docker.

```bash
# 1. Banco (PostgreSQL 16 na porta 5434 do host)
docker compose up -d

# 2. Back-end (http://localhost:8080); o Flyway cria o schema e a carga inicial na primeira subida
cd backend
mvn spring-boot:run

# 3. Front-end (http://localhost:5180); o Vite encaminha /api para o back-end
cd frontend
npm install
npm run dev
```

Testes do back-end: `cd backend && mvn test` (não precisam do Docker).

Professores de exemplo: `prof.ana`, `prof.bruno`, `prof.carla`, todos com senha `prof123` (usados a partir da Sprint 3).
