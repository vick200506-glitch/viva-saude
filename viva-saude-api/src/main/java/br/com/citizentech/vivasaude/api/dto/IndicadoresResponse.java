package br.com.citizentech.vivasaude.api.dto;

import br.com.citizentech.vivasaude.domain.Prioridade;
import java.util.List;
import java.util.Map;

/**
 * Painel da prefeitura: só números agregados.
 * Nenhum nome, telefone ou dado de saúde individual sai por aqui.
 */
public record IndicadoresResponse(
        long pacientesCadastrados,
        long pedidosNaFila,
        double tempoMedioEsperaDias,
        long consultasRealizadas,
        long cancelamentos,
        long faltas,
        long vagasReaproveitadas,
        long vagasPerdidas,
        long examesSolicitados,
        long examesAgendados,
        long examesRealizados,
        Map<String, Long> solicitacoesPorEspecialidade,
        Map<Prioridade, Long> triagensPorPrioridade,
        List<UnidadeIndicador> porUnidade) {

    public record UnidadeIndicador(String unidade, long agendadas, long realizadas, long canceladas, long faltas,
                                   long vagasReaproveitadas) {
    }
}
