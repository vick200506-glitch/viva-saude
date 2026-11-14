package br.com.citizentech.vivasaude;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Testa a gestão de vagas do começo ao fim.
 * DirtiesContext: este teste muda os dados, então o Spring recria o banco depois dele
 * (assim os outros testes começam com os dados originais).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class VagasTest {

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

    private String getJson(String url, String token) throws Exception {
        return mvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void pacienteNaoAcessaVagasDoProfissional() throws Exception {
        String maria = login("maria@vivasaude.dev", "Paciente@123");
        mvc.perform(get("/api/profissional/vagas").header("Authorization", bearer(maria)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fluxoCompletoDeVaga() throws Exception {
        String ana = login("ana@vivasaude.dev", "Profissional@123");
        String maria = login("maria@vivasaude.dev", "Paciente@123");
        String jose = login("jose@vivasaude.dev", "Paciente@123");
        String gestor = login("gestor@vivasaude.dev", "Gestor@123");

        // 1. Existe a vaga de exemplo (cardiologia cancelada pela Juliana), livre
        String vagasJson = getJson("/api/profissional/vagas", ana);
        List<Number> livres = JsonPath.read(vagasJson, "$[?(@.status=='LIVRE')].id");
        Number vagaId = livres.get(0);

        // 2. A Maria (cardiologia marcada mais tarde) está na lista de elegíveis
        String elegiveisJson = getJson("/api/profissional/vagas/" + vagaId + "/elegiveis", ana);
        List<Number> idsMaria = JsonPath.read(elegiveisJson, "$[?(@.paciente=='Maria da Silva')].id");
        Number consultaMaria = idsMaria.get(0);

        // 3. Oferecer a vaga para um pedido de OUTRA especialidade é recusado (409)
        String agendaJson = getJson("/api/profissional/agenda", ana);
        List<Number> ortopedia = JsonPath.read(agendaJson, "$[?(@.especialidade=='Ortopedia')].id");
        mvc.perform(post("/api/profissional/vagas/" + vagaId + "/oferecer")
                        .header("Authorization", bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consultaId\":" + ortopedia.get(0) + "}"))
                .andExpect(status().isConflict());

        // 4. Oferece para a Maria
        mvc.perform(post("/api/profissional/vagas/" + vagaId + "/oferecer")
                        .header("Authorization", bearer(ana))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"consultaId\":" + consultaMaria + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OFERECIDA"));

        // 5. A Maria vê a oferta no app dela
        mvc.perform(get("/api/paciente/vagas-oferecidas").header("Authorization", bearer(maria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // 6. O José não consegue aceitar a vaga oferecida para a Maria (404)
        mvc.perform(post("/api/paciente/vagas/" + vagaId + "/aceitar").header("Authorization", bearer(jose)))
                .andExpect(status().isNotFound());

        // 7. A Maria aceita
        mvc.perform(post("/api/paciente/vagas/" + vagaId + "/aceitar").header("Authorization", bearer(maria)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PREENCHIDA"));

        // 8. O horário antigo da Maria virou uma nova vaga (efeito cascata)
        mvc.perform(get("/api/profissional/vagas").header("Authorization", bearer(ana)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Antecipação")));

        // 9. A consulta da Maria agora está marcada como reaproveitada
        mvc.perform(get("/api/paciente/consultas").header("Authorization", bearer(maria)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"reaproveitada\":true")));

        // 10. A prefeitura vê 1 vaga reaproveitada
        mvc.perform(get("/api/prefeitura/indicadores").header("Authorization", bearer(gestor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vagasReaproveitadas").value(1));
    }
}
