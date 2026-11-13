package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.NotBlank;

public record NovaConsultaRequest(@NotBlank String especialidade) {
}
