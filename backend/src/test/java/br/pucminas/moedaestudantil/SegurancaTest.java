package br.pucminas.moedaestudantil;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.pucminas.moedaestudantil.service.NotificacaoService;

/**
 * Sprint 3: autenticacao com o token JWT real emitido pelo login e as regras "so o proprio dono"
 * aplicadas na API (nao so na tela).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SegurancaTest {

    private static final byte[] PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @MockitoBean NotificacaoService notificacao;

    long maria;
    long joao;
    long cantina;
    long livraria;

    @BeforeEach
    void preparar() throws Exception {
        maria = criarAluno("Maria Silva", "52998224725", "maria");
        joao = criarAluno("João Pereira", "11144477735", "joao");
        cantina = criarEmpresa("Cantina", "11222333000181", "cantina");
        livraria = criarEmpresa("Livraria", "11444777000161", "livraria");
    }

    private long criarAluno(String nome, String cpf, String login) throws Exception {
        long puc = jdbc.queryForObject("SELECT id FROM instituicao_ensino WHERE nome = 'PUC Minas'", Long.class);
        return id(mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"%s","email":"%s@email.com","cpf":"%s","rg":"MG-1","curso":"ES",
                 "endereco":{"logradouro":"Rua A","numero":"1","bairro":"B","cidade":"BH","uf":"MG","cep":"30535901"},
                 "instituicaoId":%d,"login":"%s","senha":"segredo1"}
                """.formatted(nome, login, cpf, puc, login))).andExpect(status().isCreated()));
    }

    private long criarEmpresa(String nome, String cnpj, String login) throws Exception {
        return id(mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeFantasia":"%s","cnpj":"%s","email":"%s@empresa.com","login":"%s","senha":"segredo1"}
                """.formatted(nome, cnpj, login, login))).andExpect(status().isCreated()));
    }

    private static long id(org.springframework.test.web.servlet.ResultActions r) throws Exception {
        return ((Number) JsonPath.read(r.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    /** Faz login de verdade e devolve o cabecalho "Bearer <token>". */
    private String bearer(String login, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"%s\",\"senha\":\"%s\"}".formatted(login, senha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(corpo, "$.token");
    }

    @Test
    void semTokenOuComTokenInvalidoRecebe401() throws Exception {
        mvc.perform(get("/api/alunos/" + maria))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Faça login para continuar (sessão ausente ou expirada)"));
        mvc.perform(get("/api/alunos/" + maria).header(HttpHeaders.AUTHORIZATION, "Bearer token.falso.qualquer"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotasPublicasNaoExigemLogin() throws Exception {
        mvc.perform(get("/api/instituicoes")).andExpect(status().isOk());
        mvc.perform(get("/api/vantagens")).andExpect(status().isOk());
    }

    @Test
    void alunoSoAcessaOProprioCadastroExtratoEResgate() throws Exception {
        String tokenMaria = bearer("maria", "segredo1");

        mvc.perform(get("/api/alunos/" + maria).header(HttpHeaders.AUTHORIZATION, tokenMaria))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
        mvc.perform(get("/api/alunos/" + joao).header(HttpHeaders.AUTHORIZATION, tokenMaria))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Você não tem permissão para acessar este recurso"));
        mvc.perform(get("/api/alunos/" + joao + "/extrato").header(HttpHeaders.AUTHORIZATION, tokenMaria))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/alunos/" + joao + "/resgates").header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"vantagemId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void alunoNaoUsaFuncoesDeProfessorNemProfessorDeOutro() throws Exception {
        long profAna = jdbc.queryForObject("SELECT id FROM usuario WHERE login = 'prof.ana'", Long.class);
        long profBruno = jdbc.queryForObject("SELECT id FROM usuario WHERE login = 'prof.bruno'", Long.class);
        String envio = "{\"alunoId\":%d,\"valor\":10,\"motivo\":\"x\"}".formatted(maria);

        // aluno tentando enviar moedas "como" a professora
        mvc.perform(post("/api/professores/" + profAna + "/envios").header(HttpHeaders.AUTHORIZATION, bearer("maria", "segredo1"))
                        .contentType(MediaType.APPLICATION_JSON).content(envio))
                .andExpect(status().isForbidden());
        // professor tentando gastar o saldo de outro professor
        mvc.perform(post("/api/professores/" + profBruno + "/envios").header(HttpHeaders.AUTHORIZATION, bearer("prof.ana", "prof123"))
                        .contentType(MediaType.APPLICATION_JSON).content(envio))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/professores/" + profAna + "/envios").header(HttpHeaders.AUTHORIZATION, bearer("prof.ana", "prof123"))
                        .contentType(MediaType.APPLICATION_JSON).content(envio))
                .andExpect(status().isCreated());
    }

    @Test
    void professorVeSoResumoDosAlunosSemDadosPessoais() throws Exception {
        long profAna = jdbc.queryForObject("SELECT id FROM usuario WHERE login = 'prof.ana'", Long.class);
        mvc.perform(get("/api/professores/" + profAna + "/alunos").header(HttpHeaders.AUTHORIZATION, bearer("prof.ana", "prof123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[0].saldo").exists())
                .andExpect(jsonPath("$[0].cpf").doesNotExist())
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].endereco").doesNotExist());
    }

    @Test
    void empresaSoGerenciaAsPropriasVantagens() throws Exception {
        String tokenCantina = bearer("cantina", "segredo1");
        String tokenLivraria = bearer("livraria", "segredo1");
        MockMultipartFile dados = new MockMultipartFile("dados", "", "application/json",
                "{\"nome\":\"Almoço\",\"descricao\":\"Almoço no RU\",\"custoMoedas\":50}".getBytes());
        MockMultipartFile foto = new MockMultipartFile("foto", "a.png", "image/png", PNG);

        long vantagem = id(mvc.perform(multipart("/api/empresas/" + cantina + "/vantagens").file(dados).file(foto)
                .header(HttpHeaders.AUTHORIZATION, tokenCantina)).andExpect(status().isCreated()));

        mvc.perform(multipart("/api/empresas/" + cantina + "/vantagens").file(dados).file(foto)
                        .header(HttpHeaders.AUTHORIZATION, tokenLivraria))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/vantagens/" + vantagem + "/desativar").header(HttpHeaders.AUTHORIZATION, tokenLivraria))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/empresas/" + cantina + "/resgates").header(HttpHeaders.AUTHORIZATION, tokenLivraria))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/vantagens/" + vantagem + "/desativar").header(HttpHeaders.AUTHORIZATION, tokenCantina))
                .andExpect(status().isOk());
    }

    @Test
    void trocarSenhaExigeSenhaAtualCorreta() throws Exception {
        long puc = jdbc.queryForObject("SELECT id FROM instituicao_ensino WHERE nome = 'PUC Minas'", Long.class);
        String tokenMaria = bearer("maria", "segredo1");
        String base = """
                {"nome":"Maria Silva","email":"maria@email.com","cpf":"52998224725","rg":"MG-1","curso":"ES",
                 "endereco":{"logradouro":"Rua A","numero":"1","bairro":"B","cidade":"BH","uf":"MG","cep":"30535901"},
                 "instituicaoId":%d,"login":"maria","senha":"novaSenha9","senhaAtual":%s}
                """;

        mvc.perform(put("/api/alunos/" + maria).header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON).content(base.formatted(puc, "\"errada\"")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Senha atual incorreta"));
        mvc.perform(put("/api/alunos/" + maria).header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON).content(base.formatted(puc, "null")))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(put("/api/alunos/" + maria).header(HttpHeaders.AUTHORIZATION, tokenMaria)
                        .contentType(MediaType.APPLICATION_JSON).content(base.formatted(puc, "\"segredo1\"")))
                .andExpect(status().isOk());

        bearer("maria", "novaSenha9");
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"maria\",\"senha\":\"segredo1\"}"))
                .andExpect(status().isUnauthorized());
    }
}
