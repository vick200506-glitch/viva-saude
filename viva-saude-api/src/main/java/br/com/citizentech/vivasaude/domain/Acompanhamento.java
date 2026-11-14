package br.com.citizentech.vivasaude.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

/** Uma medida registrada pelo paciente (pressão, glicemia ou peso). */
@Entity
public class Acompanhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMedida tipo;

    @Column(nullable = false)
    private String valor;

    @Column(nullable = false)
    private LocalDate data;

    protected Acompanhamento() {
    }

    public Acompanhamento(Paciente paciente, TipoMedida tipo, String valorDigitado) {
        this.paciente = paciente;
        this.tipo = tipo;
        this.valor = tipo.validar(valorDigitado); // valor impossível nem chega a ser criado
        this.data = LocalDate.now();
    }

    /** Usado apenas pelos dados de exemplo. */
    public void definirData(LocalDate data) {
        this.data = data;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public TipoMedida getTipo() { return tipo; }
    public String getValor() { return valor; }
    public LocalDate getData() { return data; }
}
