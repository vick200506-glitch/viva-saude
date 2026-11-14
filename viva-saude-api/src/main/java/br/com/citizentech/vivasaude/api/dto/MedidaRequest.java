package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.TipoMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** tipo: PRESSAO_ARTERIAL, GLICEMIA ou PESO. valor: "130/80", "98" ou "72,5". */
public record MedidaRequest(@NotNull TipoMedida tipo, @NotBlank @Size(max = 20) String valor) {
}
