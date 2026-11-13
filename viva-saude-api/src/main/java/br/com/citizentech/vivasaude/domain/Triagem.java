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
import java.util.List;

@Entity
public class Triagem {

    private static final String SEPARADOR = ";";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Paciente paciente;

    private String area;
    private String especialidade;

    @Column(length = 2000)
    private String sintomas;

    @Column(length = 2000)
    private String alertas;

    private String duracao;

    @Column(length = 1000)
    private String relato;

    @Enumerated(EnumType.STRING)
    private Prioridade sugerida;

    @Enumerated(EnumType.STRING)
    private Prioridade classificacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusTriagem status;

    @Column(nullable = false)
    private LocalDate criadaEm;

    protected Triagem() {
    }

    public Triagem(Paciente paciente, String area, String especialidade, List<String> sintomas,
                   List<String> alertas, String duracao, String relato) {
        List<String> s = sintomas == null ? List.of() : sintomas;
        List<String> a = alertas == null ? List.of() : alertas;
        this.paciente = paciente;
        this.area = area;
        this.especialidade = especialidade;
        this.sintomas = String.join(SEPARADOR, s);
        this.alertas = String.join(SEPARADOR, a);
        this.duracao = duracao;
        this.relato = relato;
        this.sugerida = sugerirPrioridade(s, a, duracao);
        this.status = StatusTriagem.AGUARDANDO;
        this.criadaEm = LocalDate.now();
    }

    /**
     * Sugestão automática de prioridade (apoio à equipe, não é diagnóstico).
     * A classificação final é sempre feita por um profissional em avaliar().
     */
    public static Prioridade sugerirPrioridade(List<String> sintomas, List<String> alertas, String duracao) {
        if (!alertas.isEmpty()) {
            return Prioridade.VERMELHO;
        }
        if (sintomas.size() >= 3 || "Mais de 1 mês".equals(duracao)) {
            return Prioridade.AMARELO;
        }
        return Prioridade.VERDE;
    }

    public void avaliar(Prioridade classificacao, boolean encaminhar) {
        if (status != StatusTriagem.AGUARDANDO) {
            throw new RegraDeNegocioException("Esta triagem já foi avaliada.");
        }
        this.classificacao = classificacao;
        this.status = encaminhar ? StatusTriagem.ENCAMINHADA : StatusTriagem.ORIENTADA;
    }

    private static List<String> separar(String texto) {
        return (texto == null || texto.isBlank()) ? List.of() : List.of(texto.split(SEPARADOR));
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getArea() { return area; }
    public String getEspecialidade() { return especialidade; }
    public List<String> getSintomas() { return separar(sintomas); }
    public List<String> getAlertas() { return separar(alertas); }
    public String getDuracao() { return duracao; }
    public String getRelato() { return relato; }
    public Prioridade getSugerida() { return sugerida; }
    public Prioridade getClassificacao() { return classificacao; }
    public StatusTriagem getStatus() { return status; }
    public LocalDate getCriadaEm() { return criadaEm; }
}
