package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Paciente;
import java.time.LocalDate;

public record PacienteResponse(Long id, String nome, LocalDate nascimento, String cartaoSus,
                               String telefone, String bairro) {

    public static PacienteResponse from(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getNascimento(), p.getCartaoSus(),
                p.getTelefone(), p.getBairro());
    }
}
