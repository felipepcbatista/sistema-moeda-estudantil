package br.pucminas.moedaestudantil;

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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmpresaParceiraControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    private static String json(String nome, String cnpj, String email, String login, String senha) {
        String campoSenha = senha == null ? "null" : "\"" + senha + "\"";
        return """
                {"nomeFantasia":"%s","cnpj":"%s","email":"%s","login":"%s","senha":%s}
                """.formatted(nome, cnpj, email, login, campoSenha);
    }

    private long cadastrar() throws Exception {
        String body = mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina Coração", "11.222.333/0001-81", "contato@cantina.com", "cantina", "segredo1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("11222333000181"))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void crudCompletoDeEmpresa() throws Exception {
        long id = cadastrar();

        mvc.perform(get("/api/empresas/" + id).with(ComoUsuario.empresa(id)))
                .andExpect(jsonPath("$.nomeFantasia").value("Cantina Coração"));

        mvc.perform(put("/api/empresas/" + id).with(ComoUsuario.empresa(id)).contentType(MediaType.APPLICATION_JSON)
                        .content(json("Cantina do Coração", "11222333000181", "contato@cantina.com", "cantina", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeFantasia").value("Cantina do Coração"));

        mvc.perform(delete("/api/empresas/" + id).with(ComoUsuario.empresa(id))).andExpect(status().isNoContent());
        mvc.perform(get("/api/empresas/" + id).with(ComoUsuario.empresa(id))).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaCnpjInvalidoEDuplicado() throws Exception {
        mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("X", "11.222.333/0001-00", "x@x.com", "xxxx", "segredo1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpj").value("CNPJ inválido"));

        cadastrar();
        mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Outra", "11222333000181", "outra@x.com", "outra", "segredo1")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("CNPJ já cadastrado"));
    }

    @Test
    void excluiEmpresaLevandoVantagensSemResgate() throws Exception {
        long id = cadastrar();
        jdbc.update("INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id) "
                + "VALUES ('Almoço', 'Desconto de 20%', '/fotos/almoco.jpg', 50, TRUE, ?)", id);

        mvc.perform(delete("/api/empresas/" + id).with(ComoUsuario.empresa(id))).andExpect(status().isNoContent());
    }

    @Test
    void naoExcluiEmpresaComVantagemResgatada() throws Exception {
        long id = cadastrar();
        jdbc.update("INSERT INTO vantagem (nome, descricao, foto_url, custo_moedas, ativa, empresa_id) "
                + "VALUES ('Almoço', 'Desconto de 20%', '/fotos/almoco.jpg', 50, TRUE, ?)", id);
        long vantagemId = jdbc.queryForObject("SELECT id FROM vantagem WHERE empresa_id = ?", Long.class, id);
        jdbc.update("INSERT INTO conta (saldo) VALUES (0)");
        long conta = jdbc.queryForObject("SELECT MAX(id) FROM conta", Long.class);
        jdbc.update("INSERT INTO transacao (tipo, valor, data_hora, conta_origem_id, codigo, vantagem_id) "
                + "VALUES ('RESGATE', 50, CURRENT_TIMESTAMP, ?, 'ABC123', ?)", conta, vantagemId);

        mvc.perform(delete("/api/empresas/" + id).with(ComoUsuario.empresa(id)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Empresa possui vantagens já resgatadas e não pode ser excluída"));
    }
}
