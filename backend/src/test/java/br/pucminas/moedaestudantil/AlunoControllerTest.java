package br.pucminas.moedaestudantil;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlunoControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    EntityManager em;

    private long instituicaoId() {
        return jdbc.queryForObject("SELECT id FROM instituicao_ensino WHERE nome = 'PUC Minas'", Long.class);
    }

    private String json(String cpf, String email, String login, String senha) {
        String campoSenha = senha == null ? "null" : "\"" + senha + "\"";
        return """
                {"nome":"Maria Silva","email":"%s","cpf":"%s","rg":"MG-12.345.678","curso":"Engenharia de Software",
                 "endereco":{"logradouro":"Rua Dom José Gaspar","numero":"500","complemento":"Prédio 1",
                             "bairro":"Coração Eucarístico","cidade":"Belo Horizonte","uf":"mg","cep":"30535-901"},
                 "instituicaoId":%d,"login":"%s","senha":%s}
                """.formatted(email, cpf, instituicaoId(), login, campoSenha);
    }

    private long cadastrar(String cpf, String email, String login) throws Exception {
        String body = mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json(cpf, email, login, "segredo1")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void cadastraAlunoComSaldoZeroDadosNormalizadosESenhaComHash() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("529.982.247-25", "Maria@Email.com", "maria", "segredo1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("52998224725"))
                .andExpect(jsonPath("$.email").value("maria@email.com"))
                .andExpect(jsonPath("$.endereco.uf").value("MG"))
                .andExpect(jsonPath("$.endereco.cep").value("30535901"))
                .andExpect(jsonPath("$.instituicao.nome").value("PUC Minas"))
                .andExpect(jsonPath("$.saldo").value(0))
                .andExpect(jsonPath("$.senha").doesNotExist());

        String hash = jdbc.queryForObject("SELECT senha_hash FROM usuario WHERE login = 'maria'", String.class);
        assertThat(hash).startsWith("$2a$").isNotEqualTo("segredo1");
    }

    @Test
    void rejeitaCamposObrigatoriosECpfInvalido() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"cpf\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cpf").value("CPF inválido"))
                .andExpect(jsonPath("$.campos.endereco").exists())
                .andExpect(jsonPath("$.campos.instituicaoId").exists());
    }

    @Test
    void exigeSenhaNoCadastro() throws Exception {
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("52998224725", "a@a.com", "maria", null)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Senha é obrigatória no cadastro"));
    }

    @Test
    void bloqueiaCpfEmailELoginDuplicados() throws Exception {
        cadastrar("52998224725", "maria@email.com", "maria");

        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("529.982.247-25", "outra@email.com", "outra", "segredo1")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("CPF já cadastrado"));
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("11144477735", "MARIA@email.com", "outra", "segredo1")))
                .andExpect(jsonPath("$.mensagem").value("Email já cadastrado"));
        // login de professor pre-cadastrado: unicidade vale entre todos os perfis
        mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON)
                        .content(json("11144477735", "outra@email.com", "prof.ana", "segredo1")))
                .andExpect(jsonPath("$.mensagem").value("Login já está em uso"));
    }

    @Test
    void consultaAtualizaSemTrocarSenhaEExclui() throws Exception {
        long id = cadastrar("52998224725", "maria@email.com", "maria");
        String hashAntes = jdbc.queryForObject("SELECT senha_hash FROM usuario WHERE id = ?", String.class, id);

        mvc.perform(get("/api/alunos/" + id).with(ComoUsuario.aluno(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("maria"));

        String edicao = json("52998224725", "maria.nova@email.com", "maria", null).replace("Maria Silva", "Maria S. Costa");
        mvc.perform(put("/api/alunos/" + id).with(ComoUsuario.aluno(id)).contentType(MediaType.APPLICATION_JSON).content(edicao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria S. Costa"))
                .andExpect(jsonPath("$.email").value("maria.nova@email.com"));
        String hashDepois = jdbc.queryForObject("SELECT senha_hash FROM usuario WHERE id = ?", String.class, id);
        assertThat(hashDepois).isEqualTo(hashAntes);

        mvc.perform(delete("/api/alunos/" + id).with(ComoUsuario.aluno(id))).andExpect(status().isNoContent());
        em.flush(); // o teste roda numa unica transacao; forca o DELETE antes de conferir via JDBC
        mvc.perform(get("/api/alunos/" + id).with(ComoUsuario.aluno(id)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem", startsWith("Aluno " + id)));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE id = ?", Integer.class, id)).isZero();
    }

    @Test
    void naoExcluiAlunoComMovimentacao() throws Exception {
        long id = cadastrar("52998224725", "maria@email.com", "maria");
        long contaAluno = jdbc.queryForObject("SELECT conta_id FROM aluno WHERE id = ?", Long.class, id);
        long contaProf = jdbc.queryForObject(
                "SELECT p.conta_id FROM professor p JOIN usuario u ON u.id = p.id WHERE u.login = 'prof.ana'", Long.class);
        jdbc.update("INSERT INTO transacao (tipo, valor, data_hora, conta_origem_id, conta_destino_id, motivo) "
                + "VALUES ('ENVIO', 10, CURRENT_TIMESTAMP, ?, ?, 'Participação')", contaProf, contaAluno);

        mvc.perform(delete("/api/alunos/" + id).with(ComoUsuario.aluno(id)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Aluno possui movimentações de moedas e não pode ser excluído"));
    }

    @Test
    void listaInstituicoesPreCadastradas() throws Exception {
        mvc.perform(get("/api/instituicoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].nome").value("CEFET-MG"));
    }
}
