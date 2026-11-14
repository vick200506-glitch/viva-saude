package br.com.citizentech.vivasaude.service;

import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Profissional;
import br.com.citizentech.vivasaude.domain.RegraDeNegocioException;
import br.com.citizentech.vivasaude.domain.StatusConsulta;
import br.com.citizentech.vivasaude.domain.Vaga;
import br.com.citizentech.vivasaude.repository.ConsultaRepository;
import br.com.citizentech.vivasaude.repository.VagaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Regras da gestão de vagas que envolvem mais de uma entidade (consultas E vagas).
 * Por isso ficam num Service, e não dentro de uma entidade só.
 */
@Service
public class VagaService {

    private final VagaRepository vagas;
    private final ConsultaRepository consultas;

    public VagaService(VagaRepository vagas, ConsultaRepository consultas) {
        this.vagas = vagas;
        this.consultas = consultas;
    }

    /** Cancela a consulta. Se ela tinha horário marcado no futuro, esse horário vira uma vaga livre. */
    @Transactional
    public Consulta cancelar(Consulta consulta) {
        boolean tinhaHorario = consulta.getStatus() == StatusConsulta.AGENDADA;
        consulta.cancelar();
        consultas.save(consulta);
        if (tinhaHorario && !consulta.getData().isBefore(LocalDate.now())) {
            vagas.save(new Vaga(consulta.getProfissional(), consulta.getEspecialidade(),
                    consulta.getData(), consulta.getHorario(), "Cancelamento"));
        }
        return consulta;
    }

    /**
     * Quem pode ficar com a vaga: mesma especialidade, sem horário ou com horário mais tarde,
     * e que ainda não recusou. Ordem: prioridade > quem não tem horário > quem pediu antes.
     */
    public List<Consulta> elegiveis(Vaga vaga) {
        return consultas.findByEspecialidadeAndStatusIn(vaga.getEspecialidade(),
                        List.of(StatusConsulta.SOLICITADA, StatusConsulta.AGENDADA)).stream()
                .filter(c -> c.getStatus() == StatusConsulta.SOLICITADA || c.getData().isAfter(vaga.getData()))
                .filter(c -> !vaga.getRecusas().contains(c.getId()))
                .sorted(Comparator.comparing(Consulta::getPrioridade)
                        .thenComparing(c -> c.getStatus() == StatusConsulta.SOLICITADA ? 0 : 1)
                        .thenComparing(Consulta::getCriadaEm))
                .toList();
    }

    @Transactional
    public Vaga oferecer(Long vagaId, Long consultaId) {
        Vaga vaga = buscar(vagaId);
        Consulta consulta = elegiveis(vaga).stream()
                .filter(c -> c.getId().equals(consultaId))
                .findFirst()
                .orElseThrow(() -> new RegraDeNegocioException("Este pedido não é elegível para esta vaga."));
        vaga.oferecer(consulta);
        return vagas.save(vaga);
    }

    /**
     * Registra a resposta do paciente. Se aceitou, a consulta dele vai para a vaga e,
     * se ele tinha um horário mais tarde, esse horário antigo vira uma nova vaga (efeito cascata).
     */
    @Transactional
    public Vaga responder(Vaga vaga, boolean aceitou) {
        if (!aceitou) {
            vaga.registrarRecusa();
            return vagas.save(vaga);
        }
        Consulta consulta = vaga.getOferta();
        if (consulta == null) {
            throw new RegraDeNegocioException("Esta vaga não está aguardando resposta.");
        }
        // Guarda o horário antigo ANTES de mudar a consulta
        boolean tinhaHorario = consulta.getStatus() == StatusConsulta.AGENDADA;
        Profissional profissionalAntigo = consulta.getProfissional();
        LocalDate dataAntiga = consulta.getData();
        LocalTime horarioAntigo = consulta.getHorario();

        consulta.ocuparVaga(vaga.getProfissional(), vaga.getData(), vaga.getHorario());
        vaga.preencher();
        consultas.save(consulta);
        vagas.save(vaga);

        if (tinhaHorario && !dataAntiga.isBefore(LocalDate.now())) {
            vagas.save(new Vaga(profissionalAntigo, consulta.getEspecialidade(), dataAntiga, horarioAntigo, "Antecipação"));
        }
        return vaga;
    }

    public Vaga buscar(Long id) {
        return vagas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vaga não encontrada."));
    }
}
