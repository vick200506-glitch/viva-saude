package br.com.citizentech.vivasaude;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/** Etapa 4: exames, medidas de acompanhamento e histórico. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ExamesTest {

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

    private MockHttpServletRequestBuilder postJson(String url, String token, String json) {
        return post(url).header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(json);
    }

    @Test
    void pacienteNaoAcessaExamesDoProfissional() throws Exception {
        String maria = login("maria@vivasaude.dev", "Paciente@123");
        mvc.perform(get("/api/profissional/exames").header("Authorization", "Bearer " + maria))
                .andExpect(status().isForbidden());
    }

    @Test
    void fluxoCompletoDeExame() throws Exception {
        String maria = login("maria@vivasaude.dev", "Paciente@123");
        String ana = login("ana@vivasaude.dev", "Profissional@123");
        String gestor = login("gestor@vivasaude.dev", "Gestor@123");

        // Exame que não existe na lista: 400
        mvc.perform(postJson("/api/paciente/exames", maria, "{\"nome\":\"Exame inventado\"}"))
                .andExpect(status().isBadRequest());

        // 1. Maria pede um raio-X
        String corpo = mvc.perform(postJson("/api/paciente/exames", maria, "{\"nome\":\"Raio-X\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADO"))
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(corpo, "$.id");

        // 2. Resultado antes de agendar não pode (409)
        mvc.perform(postJson("/api/profissional/exames/" + id + "/resultado", ana, "{\"resultado\":\"Sem alterações\"}"))
                .andExpect(status().isConflict());

        // 3. Ana agenda
        String daquiDoisDias = LocalDate.now().plusDays(2).toString();
        mvc.perform(postJson("/api/profissional/exames/" + id + "/agendar", ana,
                        "{\"data\":\"" + daquiDoisDias + "\",\"unidade\":\"UBS Centro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGENDADO"));

        // 4. Ana registra o resultado
        mvc.perform(postJson("/api/profissional/exames/" + id + "/resultado", ana, "{\"resultado\":\"Sem alterações\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REALIZADO"));

        // 5. Maria vê o resultado e o exame entra no histórico dela
        mvc.perform(get("/api/paciente/exames").header("Authorization", "Bearer " + maria))
                .andExpect(content().string(containsString("Sem alterações")));
        mvc.perform(get("/api/paciente/historico").header("Authorization", "Bearer " + maria))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Raio-X: Sem alterações")));

        // 6. A prefeitura vê só os totais: o hemograma de exemplo + este raio-X
        mvc.perform(get("/api/prefeitura/indicadores").header("Authorization", "Bearer " + gestor))
                .andExpect(jsonPath("$.examesRealizados").value(2))
                .andExpect(content().string(not(containsString("Raio-X"))));
    }

    @Test
    void medidasSaoValidadasEIsoladas() throws Exception {
        String maria = login("maria@vivasaude.dev", "Paciente@123");
        String jose = login("jose@vivasaude.dev", "Paciente@123");
        String ana = login("ana@vivasaude.dev", "Profissional@123");

        // Valor válido, com vírgula: é padronizado
        mvc.perform(postJson("/api/paciente/acompanhamentos", maria, "{\"tipo\":\"PESO\",\"valor\":\"70,4\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value("70.4"));

        // Valores impossíveis: 400
        mvc.perform(postJson("/api/paciente/acompanhamentos", maria, "{\"tipo\":\"PESO\",\"valor\":\"5000\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(postJson("/api/paciente/acompanhamentos", maria, "{\"tipo\":\"PRESSAO_ARTERIAL\",\"valor\":\"abc\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(postJson("/api/paciente/acompanhamentos", maria, "{\"tipo\":\"PRESSAO_ARTERIAL\",\"valor\":\"80/120\"}"))
                .andExpect(status().isBadRequest());

        // O José não vê as medidas da Maria
        mvc.perform(get("/api/paciente/acompanhamentos").header("Authorization", "Bearer " + jose))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("70.4"))))
                .andExpect(content().string(not(containsString("150/95"))));

        // A profissional vê as medidas da Maria
        String me = mvc.perform(get("/api/paciente/me").header("Authorization", "Bearer " + maria))
                .andReturn().getResponse().getContentAsString();
        Number idMaria = JsonPath.read(me, "$.id");
        mvc.perform(get("/api/profissional/pacientes/" + idMaria + "/acompanhamentos").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("70.4")))
                .andExpect(content().string(containsString("150/95")));
    }
}
