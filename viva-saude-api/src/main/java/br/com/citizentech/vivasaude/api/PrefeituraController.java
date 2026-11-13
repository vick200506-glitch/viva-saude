package br.com.citizentech.vivasaude.api;

import br.com.citizentech.vivasaude.api.dto.IndicadoresResponse;
import br.com.citizentech.vivasaude.api.dto.IndicadoresResponse.UnidadeIndicador;
import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Prioridade;
import br.com.citizentech.vivasaude.domain.StatusConsulta;
import br.com.citizentech.vivasaude.domain.Triagem;
import br.com.citizentech.vivasaude.repository.ConsultaRepository;
import br.com.citizentech.vivasaude.repository.PacienteRepository;
import br.com.citizentech.vivasaude.repository.TriagemRepository;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Painel da prefeitura. Só o perfil GESTOR entra aqui.
 * Devolve apenas números agregados: o gestor planeja a rede sem ver dados pessoais.
 */
@RestController
@RequestMapping("/api/prefeitura")
public class PrefeituraController {

    private final ConsultaRepository consultas;
    private final TriagemRepository triagens;
    private final PacienteRepository pacientes;

    public PrefeituraController(ConsultaRepository consultas, TriagemRepository triagens, PacienteRepository pacientes) {
        this.consultas = consultas;
        this.triagens = triagens;
        this.pacientes = pacientes;
    }

    @GetMapping("/indicadores")
    public IndicadoresResponse indicadores() {
        List<Consulta> todas = consultas.findAll();

        Map<StatusConsulta, Long> porStatus = new EnumMap<>(StatusConsulta.class);
        Map<String, Long> porEspecialidade = new TreeMap<>();
        Map<String, long[]> porUnidade = new TreeMap<>();
        long somaDias = 0;
        long qtdComData = 0;

        for (Consulta c : todas) {
            porStatus.merge(c.getStatus(), 1L, Long::sum);
            porEspecialidade.merge(c.getEspecialidade(), 1L, Long::sum);

            if (c.getData() != null
                    && (c.getStatus() == StatusConsulta.AGENDADA || c.getStatus() == StatusConsulta.REALIZADA)) {
                somaDias += ChronoUnit.DAYS.between(c.getCriadaEm(), c.getData());
                qtdComData++;
            }

            if (c.getUnidade() != null) {
                long[] v = porUnidade.computeIfAbsent(c.getUnidade(), k -> new long[4]);
                switch (c.getStatus()) {
                    case AGENDADA -> v[0]++;
                    case REALIZADA -> v[1]++;
                    case CANCELADA -> v[2]++;
                    case FALTOU -> v[3]++;
                    default -> { }
                }
            }
        }

        Map<Prioridade, Long> triagensPorPrioridade = new EnumMap<>(Prioridade.class);
        for (Triagem t : triagens.findAll()) {
            Prioridade p = t.getClassificacao() != null ? t.getClassificacao() : t.getSugerida();
            triagensPorPrioridade.merge(p, 1L, Long::sum);
        }

        List<UnidadeIndicador> unidades = new ArrayList<>();
        porUnidade.forEach((nome, v) -> unidades.add(new UnidadeIndicador(nome, v[0], v[1], v[2], v[3])));

        double mediaEspera = qtdComData == 0 ? 0 : Math.round(somaDias * 10.0 / qtdComData) / 10.0;

        return new IndicadoresResponse(
                pacientes.count(),
                porStatus.getOrDefault(StatusConsulta.SOLICITADA, 0L),
                mediaEspera,
                porStatus.getOrDefault(StatusConsulta.REALIZADA, 0L),
                porStatus.getOrDefault(StatusConsulta.CANCELADA, 0L),
                porStatus.getOrDefault(StatusConsulta.FALTOU, 0L),
                porEspecialidade,
                triagensPorPrioridade,
                unidades);
    }
}
