package br.com.citizentech.vivasaude.domain;

/** Lançada quando uma ação não é permitida pelas regras (ex.: cancelar consulta já realizada). */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
