package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record AgendarRequest(@NotNull @FutureOrPresent LocalDate data, @NotNull LocalTime horario) {
}
