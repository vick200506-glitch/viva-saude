package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Prioridade;
import br.com.citizentech.vivasaude.domain.StatusConsulta;
import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultaResponse(Long id, Long pacienteId, String paciente, String especialidade,
                               String unidade, String profissional, LocalDate data, LocalTime horario,
                               StatusConsulta status, Prioridade prioridade, String origem,
                               LocalDate criadaEm) {

    public static ConsultaResponse from(Consulta c) {
        return new ConsultaResponse(
                c.getId(),
                c.getPaciente().getId(),
                c.getPaciente().getNome(),
                c.getEspecialidade(),
                c.getUnidade(),
                c.getProfissional() == null ? null : c.getProfissional().getNome(),
                c.getData(),
                c.getHorario(),
                c.getStatus(),
                c.getPrioridade(),
                c.getOrigem(),
                c.getCriadaEm());
    }
}
