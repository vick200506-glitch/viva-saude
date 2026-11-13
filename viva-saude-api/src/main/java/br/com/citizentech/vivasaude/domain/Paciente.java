package br.com.citizentech.vivasaude.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private LocalDate nascimento;
    private String cartaoSus;
    private String telefone;
    private String bairro;

    protected Paciente() {
        // exigido pelo JPA
    }

    public Paciente(String nome, LocalDate nascimento, String cartaoSus, String telefone, String bairro) {
        this.nome = nome;
        this.nascimento = nascimento;
        this.cartaoSus = cartaoSus;
        this.telefone = telefone;
        this.bairro = bairro;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public LocalDate getNascimento() { return nascimento; }
    public String getCartaoSus() { return cartaoSus; }
    public String getTelefone() { return telefone; }
    public String getBairro() { return bairro; }
}
