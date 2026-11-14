package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.Acompanhamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcompanhamentoRepository extends JpaRepository<Acompanhamento, Long> {

    /** Medidas de um paciente, da mais antiga para a mais nova (ordem do gráfico). */
    List<Acompanhamento> findByPacienteIdOrderByDataAscIdAsc(Long pacienteId);
}
