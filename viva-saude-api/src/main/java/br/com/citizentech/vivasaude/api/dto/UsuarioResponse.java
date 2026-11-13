package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Perfil;
import br.com.citizentech.vivasaude.domain.Usuario;

public record UsuarioResponse(Long id, String nome, String email, Perfil perfil) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
}
