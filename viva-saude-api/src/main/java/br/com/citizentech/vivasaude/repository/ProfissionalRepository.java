package br.com.citizentech.vivasaude.repository;

import br.com.citizentech.vivasaude.domain.Profissional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    Optional<Profissional> findFirstByEspecialidade(String especialidade);
}
