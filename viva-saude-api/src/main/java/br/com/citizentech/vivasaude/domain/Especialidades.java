package br.com.citizentech.vivasaude.domain;

import java.util.List;

/** Especialidades atendidas pela rede (as mesmas do app do cidadão). */
public final class Especialidades {

    public static final List<String> TODAS = List.of(
            "Cardiologia", "Dermatologia", "Oftalmologia", "Pediatria", "Ginecologia",
            "Ortopedia", "Saúde mental", "Odontologia", "Clínico geral");

    private Especialidades() {
    }

    public static boolean existe(String especialidade) {
        return TODAS.contains(especialidade);
    }
}
