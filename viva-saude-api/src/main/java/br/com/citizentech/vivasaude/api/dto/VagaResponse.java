package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.StatusVaga;
import br.com.citizentech.vivasaude.domain.Vaga;
import java.time.LocalDate;
import java.time.LocalTime;

public record VagaResponse(Long id, String especialidade, String unidade, String profissional, LocalDate data,
                           LocalTime horario, StatusVaga status, String origem, Long ofertaConsultaId,
                           String ofertaPaciente) {

    public static VagaResponse from(Vaga v) {
        Consulta oferta = v.getOferta();
        return new VagaResponse(v.getId(), v.getEspecialidade(), v.getUnidade(), v.getProfissional().getNome(),
                v.getData(), v.getHorario(), v.getStatus(), v.getOrigem(),
                oferta == null ? null : oferta.getId(),
                oferta == null ? null : oferta.getPaciente().getNome());
    }
}
