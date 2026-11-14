package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.StatusVaga;
import br.com.citizentech.vivasaude.domain.Vaga;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VagaRepository extends JpaRepository<Vaga, Long> {

    /** O "_" separa os campos: oferta → paciente → id. Vagas oferecidas para este paciente. */
    List<Vaga> findByStatusAndOferta_Paciente_Id(StatusVaga status, Long pacienteId);

    /** Só encontra a vaga se ela estiver oferecida PARA ESTE paciente. */
    Optional<Vaga> findByIdAndStatusAndOferta_Paciente_Id(Long id, StatusVaga status, Long pacienteId);
}
