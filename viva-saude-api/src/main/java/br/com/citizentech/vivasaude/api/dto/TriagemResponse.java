package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Prioridade;
import br.com.citizentech.vivasaude.domain.StatusTriagem;
import br.com.citizentech.vivasaude.domain.Triagem;
import java.time.LocalDate;
import java.util.List;

public record TriagemResponse(Long id, Long pacienteId, String paciente, String area, String especialidade,
                              List<String> sintomas, List<String> alertas, String duracao, String relato,
                              Prioridade sugerida, Prioridade classificacao, StatusTriagem status,
                              LocalDate criadaEm) {

    public static TriagemResponse from(Triagem t) {
        return new TriagemResponse(t.getId(), t.getPaciente().getId(), t.getPaciente().getNome(),
                t.getArea(), t.getEspecialidade(), t.getSintomas(), t.getAlertas(), t.getDuracao(),
                t.getRelato(), t.getSugerida(), t.getClassificacao(), t.getStatus(), t.getCriadaEm());
    }
}
