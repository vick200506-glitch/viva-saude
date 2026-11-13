package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.StatusConsulta;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteIdOrderByCriadaEmDesc(Long pacienteId);

    /** Busca a consulta SÓ se ela for do paciente informado. É isso que impede ver dados de outra pessoa. */
    Optional<Consulta> findByIdAndPacienteId(Long id, Long pacienteId);

    List<Consulta> findByStatus(StatusConsulta status);

    boolean existsByProfissionalIdAndDataAndHorarioAndStatus(Long profissionalId, LocalDate data,
                                                             LocalTime horario, StatusConsulta status);
}
