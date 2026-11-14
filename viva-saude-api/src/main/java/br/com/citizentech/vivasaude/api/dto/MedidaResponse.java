package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Acompanhamento;
import br.com.citizentech.vivasaude.domain.TipoMedida;
import java.time.LocalDate;

public record MedidaResponse(Long id, TipoMedida tipo, String rotulo, String valor, String unidade, LocalDate data) {

    public static MedidaResponse from(Acompanhamento a) {
        return new MedidaResponse(a.getId(), a.getTipo(), a.getTipo().getRotulo(), a.getValor(),
                a.getTipo().getUnidade(), a.getData());
    }
}
