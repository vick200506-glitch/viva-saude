package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovoExameRequest(@NotBlank String nome, @Size(max = 80) String unidade) {
}
