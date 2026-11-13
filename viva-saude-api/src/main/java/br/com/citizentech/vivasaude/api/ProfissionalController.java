package br.com.citizentech.vivasaude.api;

import br.com.citizentech.vivasaude.api.dto.AgendarRequest;
import br.com.citizentech.vivasaude.api.dto.AvaliacaoRequest;
import br.com.citizentech.vivasaude.api.dto.ComparecimentoRequest;
import br.com.citizentech.vivasaude.api.dto.ConsultaResponse;
import br.com.citizentech.vivasaude.api.dto.PacienteResponse;
import br.com.citizentech.vivasaude.api.dto.TriagemResponse;
import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Profissional;
import br.com.citizentech.vivasaude.domain.StatusConsulta;
import br.com.citizentech.vivasaude.domain.StatusTriagem;
import br.com.citizentech.vivasaude.domain.Triagem;
import br.com.citizentech.vivasaude.repository.ConsultaRepository;
import br.com.citizentech.vivasaude.repository.PacienteRepository;
import br.com.citizentech.vivasaude.repository.ProfissionalRepository;
import br.com.citizentech.vivasaude.repository.TriagemRepository;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Painel dos profissionais. Só o perfil PROFISSIONAL entra aqui. */
@RestController
@RequestMapping("/api/profissional")
public class ProfissionalController {

    private final TriagemRepository triagens;
    private final ConsultaRepository consultas;
    private final ProfissionalRepository profissionais;
    private final PacienteRepository pacientes;

    public ProfissionalController(TriagemRepository triagens, ConsultaRepository consultas,
                                  ProfissionalRepository profissionais, PacienteRepository pacientes) {
        this.triagens = triagens;
        this.consultas = consultas;
        this.profissionais = profissionais;
        this.pacientes = pacientes;
    }

    /** Triagens aguardando avaliação: as mais urgentes primeiro. */
    @GetMapping("/triagens")
    public List<TriagemResponse> triagensPendentes() {
        return triagens.findByStatus(StatusTriagem.AGUARDANDO).stream()
                .sorted(Comparator.comparing(Triagem::getSugerida).thenComparing(Triagem::getCriadaEm))
                .map(TriagemResponse::from)
                .toList();
    }

    @PostMapping("/triagens/{id}/avaliacao")
    @Transactional
    public TriagemResponse avaliar(@PathVariable Long id, @Valid @RequestBody AvaliacaoRequest req) {
        Triagem triagem = triagens.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Triagem não encontrada."));
        triagem.avaliar(req.classificacao(), req.encaminhar());
        triagens.save(triagem);
        if (req.encaminhar()) {
            consultas.save(new Consulta(triagem.getPaciente(), triagem.getEspecialidade(), req.classificacao(), "Triagem"));
        }
        return TriagemResponse.from(triagem);
    }

    /** Pedidos aguardando agendamento: prioridade primeiro, depois quem espera há mais tempo. */
    @GetMapping("/solicitacoes")
    public List<ConsultaResponse> solicitacoes() {
        return consultas.findByStatus(StatusConsulta.SOLICITADA).stream()
                .sorted(Comparator.comparing(Consulta::getPrioridade).thenComparing(Consulta::getCriadaEm))
                .map(ConsultaResponse::from)
                .toList();
    }

    @PostMapping("/consultas/{id}/agendar")
    public ConsultaResponse agendar(@PathVariable Long id, @Valid @RequestBody AgendarRequest req) {
        Consulta consulta = buscar(id);
        Profissional profissional = profissionais.findFirstByEspecialidade(consulta.getEspecialidade())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Nenhum profissional cadastrado para " + consulta.getEspecialidade() + "."));
        boolean ocupado = consultas.existsByProfissionalIdAndDataAndHorarioAndStatus(
                profissional.getId(), req.data(), req.horario(), StatusConsulta.AGENDADA);
        if (ocupado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este horário já está ocupado.");
        }
        consulta.agendar(profissional, req.data(), req.horario());
        return ConsultaResponse.from(consultas.save(consulta));
    }

    @GetMapping("/agenda")
    public List<ConsultaResponse> agenda() {
        return consultas.findByStatus(StatusConsulta.AGENDADA).stream()
                .sorted(Comparator.comparing(Consulta::getData).thenComparing(Consulta::getHorario))
                .map(ConsultaResponse::from)
                .toList();
    }

    @PatchMapping("/consultas/{id}/comparecimento")
    public ConsultaResponse comparecimento(@PathVariable Long id, @RequestBody ComparecimentoRequest req) {
        Consulta consulta = buscar(id);
        consulta.registrarComparecimento(req.compareceu());
        return ConsultaResponse.from(consultas.save(consulta));
    }

    @PatchMapping("/consultas/{id}/cancelar")
    public ConsultaResponse cancelar(@PathVariable Long id) {
        Consulta consulta = buscar(id);
        consulta.cancelar();
        return ConsultaResponse.from(consultas.save(consulta));
    }

    @GetMapping("/pacientes")
    public List<PacienteResponse> pacientes() {
        return pacientes.findAll().stream().map(PacienteResponse::from).toList();
    }

    private Consulta buscar(Long id) {
        return consultas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta não encontrada."));
    }
}
