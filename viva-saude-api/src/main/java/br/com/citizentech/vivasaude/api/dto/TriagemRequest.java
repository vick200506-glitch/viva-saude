package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TriagemRequest(
        @NotBlank @Size(max = 60) String area,
        @NotBlank String especialidade,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> sintomas,
        @Size(max = 20) List<@NotBlank @Size(max = 100) String> alertas,
        @Size(max = 40) String duracao,
        @Size(max = 1000) String relato) {
}
