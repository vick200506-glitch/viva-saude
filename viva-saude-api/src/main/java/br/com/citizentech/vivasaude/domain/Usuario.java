package br.com.citizentech.vivasaude.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Conta de acesso. Fica separada de Paciente/Profissional:
 * o login é uma coisa, os dados de saúde são outra.
 */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /** Nunca guardamos a senha em si, só o hash BCrypt dela. */
    @Column(nullable = false)
    private String senhaHash;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    /** Preenchido só quando o perfil é PACIENTE. */
    @OneToOne
    private Paciente paciente;

    /** Preenchido só quando o perfil é PROFISSIONAL. */
    @OneToOne
    private Profissional profissional;

    protected Usuario() {
    }

    public Usuario(String email, String senhaHash, String nome, Perfil perfil) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.nome = nome;
        this.perfil = perfil;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public String getNome() { return nome; }
    public Perfil getPerfil() { return perfil; }
    public Paciente getPaciente() { return paciente; }
    public Profissional getProfissional() { return profissional; }

    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public void setProfissional(Profissional profissional) { this.profissional = profissional; }
}
