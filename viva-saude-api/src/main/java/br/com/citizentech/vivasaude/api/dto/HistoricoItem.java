package br.com.citizentech.vivasaude.api.dto;

import java.time.LocalDate;

/** Um item do histórico de saúde: montado na hora a partir de consultas, exames e triagens. */
public record HistoricoItem(LocalDate data, String tipo, String descricao) {
}
