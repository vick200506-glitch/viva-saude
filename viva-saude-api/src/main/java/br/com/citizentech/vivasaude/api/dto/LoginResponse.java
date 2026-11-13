package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Perfil;
import java.time.Instant;

/** O front usa "perfil" para decidir qual painel abrir. A API confere de novo em cada pedido. */
public record LoginResponse(String token, Perfil perfil, String nome, Instant expiraEm) {
}
