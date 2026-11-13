package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Prioridade;
import jakarta.validation.constraints.NotNull;

/** encaminhar = true cria um pedido de consulta; false = orientar sem consulta. */
public record AvaliacaoRequest(@NotNull Prioridade classificacao, boolean encaminhar) {
}
