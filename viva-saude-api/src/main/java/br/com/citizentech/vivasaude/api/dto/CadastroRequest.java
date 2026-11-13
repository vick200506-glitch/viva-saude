package br.com.citizentech.vivasaude.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Cadastro público: cria SEMPRE um paciente.
 * Repare que não existe campo "perfil" aqui: ninguém consegue se cadastrar como gestor.
 */
public record CadastroRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @Past LocalDate nascimento,
        @Size(max = 30) String cartaoSus,
        @Size(max = 30) String telefone,
        @Size(max = 80) String bairro) {
}
