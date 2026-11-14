package br.com.citizentech.vivasaude.service;

import br.com.citizentech.vivasaude.api.dto.HistoricoItem;
import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Exame;
import br.com.citizentech.vivasaude.domain.StatusConsulta;
import br.com.citizentech.vivasaude.domain.StatusExame;
import br.com.citizentech.vivasaude.domain.StatusTriagem;
import br.com.citizentech.vivasaude.domain.Triagem;
import br.com.citizentech.vivasaude.repository.ConsultaRepository;
import br.com.citizentech.vivasaude.repository.ExameRepository;
import br.com.citizentech.vivasaude.repository.TriagemRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * O histórico NÃO é uma tabela: ele é montado na hora a partir do que já existe.
 * Assim cada informação tem uma única fonte da verdade (nada fica duplicado ou desatualizado).
 */
@Service
public class HistoricoService {

    private final ConsultaRepository consultas;
    private final ExameRepository exames;
    private final TriagemRepository triagens;

    public HistoricoService(ConsultaRepository consultas, ExameRepository exames, TriagemRepository triagens) {
        this.consultas = consultas;
        this.exames = exames;
        this.triagens = triagens;
    }

    public List<HistoricoItem> de(Long pacienteId) {
        List<HistoricoItem> itens = new ArrayList<>();

        for (Consulta c : consultas.findByPacienteIdOrderByCriadaEmDesc(pacienteId)) {
            if (c.getStatus() == StatusConsulta.REALIZADA) {
                String profissional = c.getProfissional() == null ? "" : " — " + c.getProfissional().getNome();
                itens.add(new HistoricoItem(c.getData(), "Consulta",
                        c.getEspecialidade() + profissional + " (" + c.getUnidade() + ")"));
            }
        }

        for (Exame e : exames.findByPacienteIdAndStatus(pacienteId, StatusExame.REALIZADO)) {
            itens.add(new HistoricoItem(e.getData(), "Exame", e.getNome() + ": " + e.getResultado()));
        }

        for (Triagem t : triagens.findByPacienteIdOrderByCriadaEmDesc(pacienteId)) {
            if (t.getStatus() != StatusTriagem.AGUARDANDO) {
                String desfecho = t.getStatus() == StatusTriagem.ENCAMINHADA
                        ? "encaminhada para " + t.getEspecialidade()
                        : "orientação enviada";
                itens.add(new HistoricoItem(t.getCriadaEm(), "Triagem", t.getArea() + ": " + desfecho));
            }
        }

        itens.sort(Comparator.comparing(HistoricoItem::data).reversed()); // mais recente primeiro
        return itens;
    }
}
