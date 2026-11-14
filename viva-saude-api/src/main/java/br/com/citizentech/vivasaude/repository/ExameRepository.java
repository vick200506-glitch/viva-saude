package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.Exame;
import br.com.citizentech.vivasaude.domain.StatusExame;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExameRepository extends JpaRepository<Exame, Long> {

    List<Exame> findByPacienteIdOrderByCriadoEmDesc(Long pacienteId);

    List<Exame> findByPacienteIdAndStatus(Long pacienteId, StatusExame status);

    List<Exame> findByStatusIn(Collection<StatusExame> status);

    long countByStatus(StatusExame status);
}
