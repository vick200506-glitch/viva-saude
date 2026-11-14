package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.NotNull;

/** Qual pedido de consulta vai receber a oferta da vaga. */
public record OfertaRequest(@NotNull Long consultaId) {
}
