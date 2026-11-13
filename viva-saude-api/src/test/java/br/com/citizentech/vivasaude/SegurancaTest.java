package br.com.citizentech.vivasaude;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testes que provam as regras de segurança.
 * Rode com: mvn test
 */
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaTest {

    @Autowired
    private MockMvc mvc;

    private String login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    @Test
    void semTokenNaoEntra() throws Exception {
        mvc.perform(get("/api/paciente/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void senhaErradaRecebe401() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@vivasaude.dev\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginDevolvePerfilESemSenha() throws Exception {
        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@vivasaude.dev\",\"senha\":\"Paciente@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("PACIENTE"))
                .andExpect(content().string(not(containsString("senha"))))
                .andExpect(content().string(not(containsString("$2a$"))));
    }

    @Test
    void tokenAlteradoEhRecusado() throws Exception {
        String token = login("maria@vivasaude.dev", "Paciente@123");
        String adulterado = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");
        mvc.perform(get("/api/paciente/me").header("Authorization", bearer(adulterado)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void cadaPerfilSoEntraNoProprioPainel() throws Exception {
        String paciente = login("maria@vivasaude.dev", "Paciente@123");
        String profissional = login("ana@vivasaude.dev", "Profissional@123");
        String gestor = login("gestor@vivasaude.dev", "Gestor@123");

        // Cada um entra no seu
        mvc.perform(get("/api/paciente/me").header("Authorization", bearer(paciente))).andExpect(status().isOk());
        mvc.perform(get("/api/profissional/triagens").header("Authorization", bearer(profissional))).andExpect(status().isOk());
        mvc.perform(get("/api/prefeitura/indicadores").header("Authorization", bearer(gestor))).andExpect(status().isOk());

        // E não entra no dos outros (403 = "sei quem você é, mas não pode")
        mvc.perform(get("/api/profissional/triagens").header("Authorization", bearer(paciente))).andExpect(status().isForbidden());
        mvc.perform(get("/api/prefeitura/indicadores").header("Authorization", bearer(paciente))).andExpect(status().isForbidden());
        mvc.perform(get("/api/paciente/me").header("Authorization", bearer(gestor))).andExpect(status().isForbidden());
        mvc.perform(get("/api/prefeitura/indicadores").header("Authorization", bearer(profissional))).andExpect(status().isForbidden());
    }

    @Test
    void pacienteSoVeAsPropriasConsultas() throws Exception {
        String maria = login("maria@vivasaude.dev", "Paciente@123");
        mvc.perform(get("/api/paciente/consultas").header("Authorization", bearer(maria)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Maria da Silva")))
                .andExpect(content().string(not(containsString("José Pereira"))));
    }

    @Test
    void pacienteNaoCancelaConsultaDeOutraPessoa() throws Exception {
        String jose = login("jose@vivasaude.dev", "Paciente@123");
        String maria = login("maria@vivasaude.dev", "Paciente@123");

        String corpo = mvc.perform(get("/api/paciente/consultas").header("Authorization", bearer(jose)))
                .andReturn().getResponse().getContentAsString();
        Number idDoJose = JsonPath.read(corpo, "$[0].id");

        // Maria tenta cancelar a consulta do José: recebe 404, como se ela não existisse
        mvc.perform(patch("/api/paciente/consultas/" + idDoJose + "/cancelar").header("Authorization", bearer(maria)))
                .andExpect(status().isNotFound());
    }

    @Test
    void prefeituraNaoRecebeDadosPessoais() throws Exception {
        String gestor = login("gestor@vivasaude.dev", "Gestor@123");
        mvc.perform(get("/api/prefeitura/indicadores").header("Authorization", bearer(gestor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacientesCadastrados").isNumber())
                .andExpect(content().string(not(containsString("Maria"))))
                .andExpect(content().string(not(containsString("99812"))));
    }

    @Test
    void cadastroPublicoSempreCriaPaciente() throws Exception {
        // Mesmo tentando mandar "perfil": "GESTOR", a conta nasce como PACIENTE
        mvc.perform(post("/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Teste","email":"novo@vivasaude.dev","senha":"SenhaForte@1","perfil":"GESTOR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("PACIENTE"));
    }
}
