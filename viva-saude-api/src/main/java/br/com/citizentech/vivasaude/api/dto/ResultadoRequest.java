package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResultadoRequest(@NotBlank @Size(max = 1000) String resultado) {
}
