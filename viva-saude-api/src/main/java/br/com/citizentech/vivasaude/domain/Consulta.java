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
import java.time.LocalTime;

@Entity
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Paciente paciente;

    @Column(nullable = false)
    private String especialidade;

    @ManyToOne
    private Profissional profissional;

    private String unidade;
    private LocalDate data;
    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusConsulta status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridade prioridade;

    private String origem;

    @Column(nullable = false)
    private LocalDate criadaEm;

    protected Consulta() {
    }

    /** Um pedido de consulta nasce sempre como SOLICITADA (aguardando agendamento). */
    public Consulta(Paciente paciente, String especialidade, Prioridade prioridade, String origem) {
        this.paciente = paciente;
        this.especialidade = especialidade;
        this.prioridade = prioridade;
        this.origem = origem;
        this.status = StatusConsulta.SOLICITADA;
        this.criadaEm = LocalDate.now();
    }

    // ---- Regras de negócio: o status só muda por estes métodos ----

    public void agendar(Profissional profissional, LocalDate data, LocalTime horario) {
        if (status != StatusConsulta.SOLICITADA) {
            throw new RegraDeNegocioException("Só pedidos aguardando agendamento podem ser agendados.");
        }
        this.profissional = profissional;
        this.unidade = profissional.getUnidade();
        this.data = data;
        this.horario = horario;
        this.status = StatusConsulta.AGENDADA;
    }

    public void cancelar() {
        if (status != StatusConsulta.SOLICITADA && status != StatusConsulta.AGENDADA) {
            throw new RegraDeNegocioException("Esta consulta não pode mais ser cancelada.");
        }
        this.status = StatusConsulta.CANCELADA;
    }

    public void registrarComparecimento(boolean compareceu) {
        if (status != StatusConsulta.AGENDADA) {
            throw new RegraDeNegocioException("Só consultas agendadas podem ter o comparecimento registrado.");
        }
        this.status = compareceu ? StatusConsulta.REALIZADA : StatusConsulta.FALTOU;
    }

    /** Usado apenas pelos dados de exemplo, para simular pedidos antigos. */
    public void definirCriadaEm(LocalDate criadaEm) {
        this.criadaEm = criadaEm;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getEspecialidade() { return especialidade; }
    public Profissional getProfissional() { return profissional; }
    public String getUnidade() { return unidade; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public StatusConsulta getStatus() { return status; }
    public Prioridade getPrioridade() { return prioridade; }
    public String getOrigem() { return origem; }
    public LocalDate getCriadaEm() { return criadaEm; }
}
