package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
}
