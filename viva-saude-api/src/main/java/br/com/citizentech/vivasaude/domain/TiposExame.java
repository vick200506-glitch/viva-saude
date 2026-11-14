package br.com.citizentech.vivasaude.domain;

import java.util.List;

/** Exames que podem ser pedidos pelo app (os mesmos da tela). */
public final class TiposExame {

    public static final List<String> TODOS = List.of(
            "Hemograma completo", "Glicemia em jejum", "Colesterol e triglicerídeos",
            "Eletrocardiograma", "Raio-X", "Ultrassom");

    private TiposExame() {
    }

    public static boolean existe(String nome) {
        return TODOS.contains(nome);
    }
}
