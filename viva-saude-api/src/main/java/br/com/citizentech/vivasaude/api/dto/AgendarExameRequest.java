package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AgendarExameRequest(@NotNull @FutureOrPresent LocalDate data, @NotBlank @Size(max = 80) String unidade) {
}
