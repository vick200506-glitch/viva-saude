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

/** Exame pedido por um paciente. Caminho: SOLICITADO → AGENDADO → REALIZADO (com resultado). */
@Entity
public class Exame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Paciente paciente;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusExame status;

    private String unidade;
    private LocalDate data;

    @Column(length = 1000)
    private String resultado;

    @Column(nullable = false)
    private LocalDate criadoEm;

    protected Exame() {
    }

    public Exame(Paciente paciente, String nome, String unidadePreferida) {
        this.paciente = paciente;
        this.nome = nome;
        this.unidade = unidadePreferida;
        this.status = StatusExame.SOLICITADO;
        this.criadoEm = LocalDate.now();
    }

    public void agendar(LocalDate data, String unidade) {
        if (status != StatusExame.SOLICITADO) {
            throw new RegraDeNegocioException("Só exames solicitados podem ser agendados.");
        }
        this.data = data;
        this.unidade = unidade;
        this.status = StatusExame.AGENDADO;
    }

    public void registrarResultado(String resultado) {
        if (status != StatusExame.AGENDADO) {
            throw new RegraDeNegocioException("Só exames agendados podem receber resultado.");
        }
        this.resultado = resultado;
        this.status = StatusExame.REALIZADO;
    }

    /** Usado apenas pelos dados de exemplo. */
    public void definirCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getNome() { return nome; }
    public StatusExame getStatus() { return status; }
    public String getUnidade() { return unidade; }
    public LocalDate getData() { return data; }
    public String getResultado() { return resultado; }
    public LocalDate getCriadoEm() { return criadoEm; }
}
