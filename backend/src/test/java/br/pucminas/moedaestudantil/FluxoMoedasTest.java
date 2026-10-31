package br.pucminas.moedaestudantil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.pucminas.moedaestudantil.model.EnvioMoedas;
import br.pucminas.moedaestudantil.model.ResgateVantagem;
import br.pucminas.moedaestudantil.service.CreditoSemestralJob;
import br.pucminas.moedaestudantil.service.NotificacaoService;
import br.pucminas.moedaestudantil.service.TransacaoService;

/** Fluxo completo: login, envio de moedas, vantagens, resgate, extrato e credito semestral. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FluxoMoedasTest {

    // PNG 1x1 valido
    private static final byte[] PNG = java.util.Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransacaoService transacaoService;
    @MockitoBean NotificacaoService notificacao;

    long profAna;   // PUC Minas
    long profCarla; // UFMG
    long aluno;     // PUC Minas
    long empresa;

    @BeforeEach
    void preparar() throws Exception {
        profAna = idDoLogin("prof.ana");
        profCarla = idDoLogin("prof.carla");
        long puc = jdbc.queryForObject("SELECT id FROM instituicao_ensino WHERE nome = 'PUC Minas'", Long.class);
        aluno = idCriado(mvc.perform(post("/api/alunos").contentType(MediaType.APPLICATION_JSON).content("""
                {"nome":"Maria Silva","email":"maria@email.com","cpf":"52998224725","rg":"MG-1","curso":"ES",
                 "endereco":{"logradouro":"Rua A","numero":"1","bairro":"B","cidade":"BH","uf":"MG","cep":"30535901"},
                 "instituicaoId":%d,"login":"maria","senha":"segredo1"}
                """.formatted(puc))).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        empresa = idCriado(mvc.perform(post("/api/empresas").contentType(MediaType.APPLICATION_JSON).content("""
                {"nomeFantasia":"Cantina","cnpj":"11222333000181","email":"c@cantina.com","login":"cantina","senha":"segredo1"}
                """)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
    }

    private long idDoLogin(String login) {
        return jdbc.queryForObject("SELECT id FROM usuario WHERE login = ?", Long.class, login);
    }

    private static long idCriado(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    private void enviar(long professor, long alunoId, int valor) throws Exception {
        mvc.perform(post("/api/professores/" + professor + "/envios").with(ComoUsuario.professor(professor)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":%d,\"valor\":%d,\"motivo\":\"Participação em aula\"}".formatted(alunoId, valor)))
                .andExpect(status().isCreated());
    }

    private long cadastrarVantagem(int custo) throws Exception {
        MockMultipartFile dados = new MockMultipartFile("dados", "", "application/json",
                "{\"nome\":\"Almoço\",\"descricao\":\"Almoço no restaurante universitário\",\"custoMoedas\":%d}"
                        .formatted(custo).getBytes());
        MockMultipartFile foto = new MockMultipartFile("foto", "almoco.png", "image/png", PNG);
        return idCriado(mvc.perform(multipart("/api/empresas/" + empresa + "/vantagens").file(dados).file(foto).with(ComoUsuario.empresa(empresa)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void loginIdentificaPerfilERecusaSenhaErrada() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"prof.ana\",\"senha\":\"prof123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
                .andExpect(jsonPath("$.nome").value("Ana Souza"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"maria\",\"senha\":\"segredo1\"}"))
                .andExpect(jsonPath("$.perfil").value("ALUNO"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"cantina\",\"senha\":\"segredo1\"}"))
                .andExpect(jsonPath("$.perfil").value("EMPRESA"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"prof.ana\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha inválidos"));
    }

    @Test
    void envioDebitaProfessorCreditaAlunoNotificaEApareceNosExtratos() throws Exception {
        mvc.perform(get("/api/professores/" + profAna + "/alunos").with(ComoUsuario.professor(profAna))).andExpect(jsonPath("$", hasSize(1)));

        enviar(profAna, aluno, 150);

        verify(notificacao).notificarRecebimento(any(EnvioMoedas.class));
        mvc.perform(get("/api/professores/" + profAna + "/extrato").with(ComoUsuario.professor(profAna)))
                .andExpect(jsonPath("$.saldo").value(850))
                .andExpect(jsonPath("$.transacoes", hasSize(2)))
                .andExpect(jsonPath("$.transacoes[0].valor").value(-150))
                .andExpect(jsonPath("$.transacoes[0].descricao").value("Enviado para Maria Silva"))
                .andExpect(jsonPath("$.transacoes[1].tipo").value("CREDITO"));
        mvc.perform(get("/api/alunos/" + aluno + "/extrato").with(ComoUsuario.aluno(aluno)))
                .andExpect(jsonPath("$.saldo").value(150))
                .andExpect(jsonPath("$.transacoes[0].valor").value(150))
                .andExpect(jsonPath("$.transacoes[0].descricao").value("Recebido de Ana Souza"))
                .andExpect(jsonPath("$.transacoes[0].motivo").value("Participação em aula"));
    }

    @Test
    void envioExigeSaldoMotivoEMesmaInstituicao() throws Exception {
        mvc.perform(post("/api/professores/" + profAna + "/envios").with(ComoUsuario.professor(profAna)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":%d,\"valor\":1001,\"motivo\":\"x\"}".formatted(aluno)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Saldo insuficiente: você tem 1000 moedas"));
        mvc.perform(post("/api/professores/" + profAna + "/envios").with(ComoUsuario.professor(profAna)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":%d,\"valor\":10,\"motivo\":\"  \"}".formatted(aluno)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.motivo").value("O motivo é obrigatório"));
        mvc.perform(post("/api/professores/" + profCarla + "/envios").with(ComoUsuario.professor(profCarla)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":%d,\"valor\":10,\"motivo\":\"x\"}".formatted(aluno)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("O aluno não pertence à instituição do professor"));

        verify(notificacao, never()).notificarRecebimento(any());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM transacao WHERE tipo = 'ENVIO'", Integer.class)).isZero();
    }

    @Test
    void empresaCadastraVantagemComFotoEExigeFoto() throws Exception {
        long vantagem = cadastrarVantagem(80);

        String fotoUrl = JsonPath.read(mvc.perform(get("/api/vantagens/" + vantagem))
                .andExpect(jsonPath("$.empresaNome").value("Cantina"))
                .andReturn().getResponse().getContentAsString(), "$.fotoUrl");
        mvc.perform(get(fotoUrl)).andExpect(status().isOk());

        MockMultipartFile dados = new MockMultipartFile("dados", "", "application/json",
                "{\"nome\":\"X\",\"descricao\":\"Y\",\"custoMoedas\":10}".getBytes());
        mvc.perform(multipart("/api/empresas/" + empresa + "/vantagens").file(dados).with(ComoUsuario.empresa(empresa)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("A foto do produto é obrigatória"));
        // tentativa de sair do diretorio de fotos: o firewall do Spring Security barra a barra codificada (400)
        // antes de chegar ao FotoStorage, que por sua vez tambem recusaria (404)
        mvc.perform(get("/api/fotos/..%2F..%2Fpom.xml")).andExpect(status().is4xxClientError());
    }

    @Test
    void resgateDebitaGeraCodigoENotificaAlunoEEmpresa() throws Exception {
        long vantagem = cadastrarVantagem(80);
        enviar(profAna, aluno, 100);

        String resposta = mvc.perform(post("/api/alunos/" + aluno + "/resgates").with(ComoUsuario.aluno(aluno)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vantagemId\":%d}".formatted(vantagem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoRestante").value(20))
                .andExpect(jsonPath("$.vantagem").value("Almoço"))
                .andReturn().getResponse().getContentAsString();
        String codigo = JsonPath.read(resposta, "$.codigo");
        assertThat(codigo).matches("[A-Z2-9]{8}");

        verify(notificacao).enviarCupomAluno(any(ResgateVantagem.class));
        verify(notificacao).notificarParceiro(any(ResgateVantagem.class));
        mvc.perform(get("/api/empresas/" + empresa + "/resgates").with(ComoUsuario.empresa(empresa)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value(codigo))
                .andExpect(jsonPath("$[0].aluno").value("Maria Silva"))
                .andExpect(jsonPath("$[0].vantagem").value("Almoço"));
        mvc.perform(get("/api/alunos/" + aluno + "/extrato").with(ComoUsuario.aluno(aluno)))
                .andExpect(jsonPath("$.transacoes[0].valor").value(-80))
                .andExpect(jsonPath("$.transacoes[0].codigo").value(codigo))
                .andExpect(jsonPath("$.transacoes[0].descricao").value("Resgate: Almoço (Cantina)"));

        // saldo 20 < custo 80
        mvc.perform(post("/api/alunos/" + aluno + "/resgates").with(ComoUsuario.aluno(aluno)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vantagemId\":%d}".formatted(vantagem)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Saldo insuficiente: a vantagem custa 80 moedas e você tem 20"));
    }

    @Test
    void vantagemDesativadaSaiDaVitrineENaoPodeSerResgatada() throws Exception {
        long vantagem = cadastrarVantagem(10);
        enviar(profAna, aluno, 50);
        mvc.perform(get("/api/vantagens")).andExpect(jsonPath("$", hasSize(1)));

        mvc.perform(post("/api/vantagens/" + vantagem + "/desativar").with(ComoUsuario.empresa(empresa))).andExpect(jsonPath("$.ativa").value(false));

        mvc.perform(get("/api/vantagens")).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/empresas/" + empresa + "/vantagens").with(ComoUsuario.empresa(empresa))).andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(post("/api/alunos/" + aluno + "/resgates").with(ComoUsuario.aluno(aluno)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vantagemId\":%d}".formatted(vantagem)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").value("Esta vantagem não está mais disponível"));
    }

    @Test
    void creditoSemestralAcumulaEEIdempotente() throws Exception {
        enviar(profAna, aluno, 300); // sobra 700

        assertThat(transacaoService.creditarSemestre("2027-1")).isEqualTo(3);
        assertThat(transacaoService.creditarSemestre("2027-1")).isZero();
        // o semestre da carga inicial ja foi creditado
        assertThat(transacaoService.creditarSemestre("2026-2")).isZero();

        mvc.perform(get("/api/professores/" + profAna).with(ComoUsuario.professor(profAna))).andExpect(jsonPath("$.saldo").value(1700));
    }

    @Test
    void calculaSemestreDaData() {
        assertThat(semestre(2027, 2)).isEqualTo("2027-1");
        assertThat(semestre(2027, 7)).isEqualTo("2027-1");
        assertThat(semestre(2027, 8)).isEqualTo("2027-2");
        assertThat(semestre(2028, 1)).isEqualTo("2027-2");
    }

    private static String semestre(int ano, int mes) {
        return CreditoSemestralJob.semestreDe(LocalDate.of(ano, mes, 1));
    }
}
