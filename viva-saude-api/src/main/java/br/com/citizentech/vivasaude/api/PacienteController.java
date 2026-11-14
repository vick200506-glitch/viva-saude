package br.com.citizentech.vivasaude.api;

import br.com.citizentech.vivasaude.api.dto.ConsultaResponse;
import br.com.citizentech.vivasaude.api.dto.NovaConsultaRequest;
import br.com.citizentech.vivasaude.api.dto.PacienteResponse;
import br.com.citizentech.vivasaude.api.dto.TriagemRequest;
import br.com.citizentech.vivasaude.api.dto.TriagemResponse;
import br.com.citizentech.vivasaude.api.dto.VagaResponse;
import br.com.citizentech.vivasaude.auth.UsuarioAtual;
import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Especialidades;
import br.com.citizentech.vivasaude.domain.Paciente;
import br.com.citizentech.vivasaude.domain.Prioridade;
import br.com.citizentech.vivasaude.domain.StatusVaga;
import br.com.citizentech.vivasaude.domain.Triagem;
import br.com.citizentech.vivasaude.domain.Vaga;
import br.com.citizentech.vivasaude.repository.ConsultaRepository;
import br.com.citizentech.vivasaude.repository.TriagemRepository;
import br.com.citizentech.vivasaude.repository.VagaRepository;
import br.com.citizentech.vivasaude.service.VagaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Painel do cidadão. Só o perfil PACIENTE entra aqui (regra no SecurityConfig).
 * Todo método descobre o paciente pelo token, então cada pessoa só vê os próprios dados.
 */
@RestController
@RequestMapping("/api/paciente")
public class PacienteController {

    private final UsuarioAtual usuarioAtual;
    private final ConsultaRepository consultas;
    private final TriagemRepository triagens;
    private final VagaRepository vagas;
    private final VagaService vagaService;

    public PacienteController(UsuarioAtual usuarioAtual, ConsultaRepository consultas, TriagemRepository triagens,
                              VagaRepository vagas, VagaService vagaService) {
        this.usuarioAtual = usuarioAtual;
        this.consultas = consultas;
        this.triagens = triagens;
        this.vagas = vagas;
        this.vagaService = vagaService;
    }

    @GetMapping("/me")
    public PacienteResponse meusDados(@AuthenticationPrincipal Jwt jwt) {
        return PacienteResponse.from(usuarioAtual.paciente(jwt));
    }

    @GetMapping("/consultas")
    public List<ConsultaResponse> minhasConsultas(@AuthenticationPrincipal Jwt jwt) {
        Paciente paciente = usuarioAtual.paciente(jwt);
        return consultas.findByPacienteIdOrderByCriadaEmDesc(paciente.getId()).stream()
                .map(ConsultaResponse::from)
                .toList();
    }

    @PostMapping("/consultas")
    @ResponseStatus(HttpStatus.CREATED)
    public ConsultaResponse pedirConsulta(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody NovaConsultaRequest req) {
        validarEspecialidade(req.especialidade());
        Consulta consulta = new Consulta(usuarioAtual.paciente(jwt), req.especialidade(), Prioridade.VERDE, "Aplicativo");
        return ConsultaResponse.from(consultas.save(consulta));
    }

    @PatchMapping("/consultas/{id}/cancelar")
    public ConsultaResponse cancelar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Paciente paciente = usuarioAtual.paciente(jwt);
        // Se a consulta for de outra pessoa, respondemos 404: nem confirmamos que ela existe.
        Consulta consulta = consultas.findByIdAndPacienteId(id, paciente.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta não encontrada."));
        return ConsultaResponse.from(vagaService.cancelar(consulta)); // se tinha horário, ele vira vaga
    }

    @GetMapping("/triagens")
    public List<TriagemResponse> minhasTriagens(@AuthenticationPrincipal Jwt jwt) {
        Paciente paciente = usuarioAtual.paciente(jwt);
        return triagens.findByPacienteIdOrderByCriadaEmDesc(paciente.getId()).stream()
                .map(TriagemResponse::from)
                .toList();
    }

    @PostMapping("/triagens")
    @ResponseStatus(HttpStatus.CREATED)
    public TriagemResponse enviarTriagem(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody TriagemRequest req) {
        validarEspecialidade(req.especialidade());
        Triagem triagem = new Triagem(usuarioAtual.paciente(jwt), req.area(), req.especialidade(),
                req.sintomas(), req.alertas(), req.duracao(), req.relato());
        return TriagemResponse.from(triagens.save(triagem));
    }

    // ---------- Vagas oferecidas a mim ----------

    @GetMapping("/vagas-oferecidas")
    public List<VagaResponse> vagasOferecidas(@AuthenticationPrincipal Jwt jwt) {
        Paciente paciente = usuarioAtual.paciente(jwt);
        return vagas.findByStatusAndOferta_Paciente_Id(StatusVaga.OFERECIDA, paciente.getId()).stream()
                .map(VagaResponse::from)
                .toList();
    }

    @PostMapping("/vagas/{id}/aceitar")
    public VagaResponse aceitarVaga(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responder(jwt, id, true);
    }

    @PostMapping("/vagas/{id}/recusar")
    public VagaResponse recusarVaga(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return responder(jwt, id, false);
    }

    private VagaResponse responder(Jwt jwt, Long id, boolean aceitou) {
        Paciente paciente = usuarioAtual.paciente(jwt);
        // Só acha a vaga se ela estiver oferecida para ESTE paciente
        Vaga vaga = vagas.findByIdAndStatusAndOferta_Paciente_Id(id, StatusVaga.OFERECIDA, paciente.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oferta não encontrada."));
        return VagaResponse.from(vagaService.responder(vaga, aceitou));
    }

    private static void validarEspecialidade(String especialidade) {
        if (!Especialidades.existe(especialidade)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Especialidade inválida. Opções: " + String.join(", ", Especialidades.TODAS));
        }
    }
}
