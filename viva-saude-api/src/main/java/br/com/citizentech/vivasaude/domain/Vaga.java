package br.com.citizentech.vivasaude.domain;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Um horário que ficou livre (por cancelamento ou antecipação) e pode ser oferecido a outro paciente.
 * O status só muda pelos métodos abaixo, que conferem as regras.
 */
@Entity
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Controle de concorrência: se duas pessoas mexerem na mesma vaga ao mesmo tempo, a segunda falha. */
    @Version
    private Long versao;

    @Column(nullable = false)
    private String especialidade;

    @ManyToOne(optional = false)
    private Profissional profissional;

    private String unidade;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private LocalTime horario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVaga status;

    /** "Cancelamento" ou "Antecipação". */
    @Column(nullable = false)
    private String origem;

    /** O pedido de consulta para quem a vaga foi oferecida (e, depois, quem ficou com ela). */
    @ManyToOne
    private Consulta oferta;

    /** Ids das consultas cujos pacientes já recusaram esta vaga: não aparecem de novo. */
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Long> recusas = new HashSet<>();

    protected Vaga() {
    }

    public Vaga(Profissional profissional, String especialidade, LocalDate data, LocalTime horario, String origem) {
        this.profissional = profissional;
        this.unidade = profissional.getUnidade();
        this.especialidade = especialidade;
        this.data = data;
        this.horario = horario;
        this.origem = origem;
        this.status = StatusVaga.LIVRE;
    }

    public void oferecer(Consulta consulta) {
        if (status != StatusVaga.LIVRE) {
            throw new RegraDeNegocioException("Esta vaga não está livre para ser oferecida.");
        }
        if (!especialidade.equals(consulta.getEspecialidade())) {
            throw new RegraDeNegocioException("O pedido é de outra especialidade.");
        }
        if (recusas.contains(consulta.getId())) {
            throw new RegraDeNegocioException("Este paciente já recusou esta vaga.");
        }
        this.oferta = consulta;
        this.status = StatusVaga.OFERECIDA;
    }

    public void registrarRecusa() {
        exigirOferecida();
        recusas.add(oferta.getId());
        this.oferta = null;
        this.status = StatusVaga.LIVRE;
    }

    public void preencher() {
        exigirOferecida();
        this.status = StatusVaga.PREENCHIDA;
    }

    private void exigirOferecida() {
        if (status != StatusVaga.OFERECIDA || oferta == null) {
            throw new RegraDeNegocioException("Esta vaga não está aguardando resposta.");
        }
    }

    public Long getId() { return id; }
    public String getEspecialidade() { return especialidade; }
    public Profissional getProfissional() { return profissional; }
    public String getUnidade() { return unidade; }
    public LocalDate getData() { return data; }
    public LocalTime getHorario() { return horario; }
    public StatusVaga getStatus() { return status; }
    public String getOrigem() { return origem; }
    public Consulta getOferta() { return oferta; }
    public Set<Long> getRecusas() { return recusas; }
}
