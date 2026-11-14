package br.com.citizentech.vivasaude.domain;

/** Lançada quando um valor digitado não faz sentido (ex.: peso de 5.000 kg). Vira HTTP 400. */
public class DadoInvalidoException extends RuntimeException {

    public DadoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
