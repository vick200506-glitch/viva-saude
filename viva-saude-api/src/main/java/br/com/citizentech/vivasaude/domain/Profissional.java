package br.com.citizentech.vivasaude.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String especialidade;

    @Column(nullable = false)
    private String unidade;

    protected Profissional() {
    }

    public Profissional(String nome, String especialidade, String unidade) {
        this.nome = nome;
        this.especialidade = especialidade;
        this.unidade = unidade;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEspecialidade() { return especialidade; }
    public String getUnidade() { return unidade; }
}
