package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Exame;
import br.com.citizentech.vivasaude.domain.StatusExame;
import java.time.LocalDate;

public record ExameResponse(Long id, Long pacienteId, String paciente, String nome, StatusExame status,
                            String unidade, LocalDate data, String resultado, LocalDate criadoEm) {

    public static ExameResponse from(Exame e) {
        return new ExameResponse(e.getId(), e.getPaciente().getId(), e.getPaciente().getNome(), e.getNome(),
                e.getStatus(), e.getUnidade(), e.getData(), e.getResultado(), e.getCriadoEm());
    }
}
