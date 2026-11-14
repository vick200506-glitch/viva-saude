package br.com.citizentech.vivasaude.domain;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tipos de medida do acompanhamento. Cada tipo sabe validar o próprio valor:
 * a regra fica junto do dado que ela protege.
 */
public enum TipoMedida {
    PRESSAO_ARTERIAL("Pressão arterial", "mmHg"),
    GLICEMIA("Glicemia", "mg/dL"),
    PESO("Peso", "kg");

    private static final Pattern PRESSAO = Pattern.compile("^(\\d{2,3})/(\\d{2,3})$");

    private final String rotulo;
    private final String unidade;

    TipoMedida(String rotulo, String unidade) {
        this.rotulo = rotulo;
        this.unidade = unidade;
    }

    public String getRotulo() { return rotulo; }
    public String getUnidade() { return unidade; }

    /** Confere o valor digitado e devolve ele padronizado. Valor impossível → DadoInvalidoException (400). */
    public String validar(String digitado) {
        String v = digitado == null ? "" : digitado.trim().replace(",", ".");
        return switch (this) {
            case PRESSAO_ARTERIAL -> validarPressao(v);
            case GLICEMIA -> numeroEntre(v, 20, 600, "glicemia");
            case PESO -> numeroEntre(v, 1, 400, "peso");
        };
    }

    private static String validarPressao(String v) {
        Matcher m = PRESSAO.matcher(v);
        if (!m.matches()) {
            throw new DadoInvalidoException("Digite a pressão assim: 130/80.");
        }
        int maxima = Integer.parseInt(m.group(1));
        int minima = Integer.parseInt(m.group(2));
        if (maxima < 60 || maxima > 260 || minima < 30 || minima > 160 || minima >= maxima) {
            throw new DadoInvalidoException("Esse valor de pressão não parece possível. Confira e digite de novo.");
        }
        return maxima + "/" + minima;
    }

    private static String numeroEntre(String v, double min, double max, String nome) {
        double n;
        try {
            n = Double.parseDouble(v);
        } catch (NumberFormatException e) {
            throw new DadoInvalidoException("Digite só números para " + nome + ". Ex.: 98 ou 72,5.");
        }
        if (Double.isNaN(n) || n < min || n > max) {
            throw new DadoInvalidoException("Esse valor de " + nome + " não parece possível. Confira e digite de novo.");
        }
        return BigDecimal.valueOf(n).stripTrailingZeros().toPlainString();
    }
}
