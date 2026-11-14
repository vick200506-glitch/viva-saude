package br.com.citizentech.vivasaude.api.dto;

/** Resposta do paciente à oferta (registrada pelo profissional, por exemplo após um telefonema). */
public record RespostaRequest(boolean aceitou) {
}
