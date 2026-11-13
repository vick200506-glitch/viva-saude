package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.StatusTriagem;
import br.com.citizentech.vivasaude.domain.Triagem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TriagemRepository extends JpaRepository<Triagem, Long> {

    List<Triagem> findByPacienteIdOrderByCriadaEmDesc(Long pacienteId);

    List<Triagem> findByStatus(StatusTriagem status);
}
