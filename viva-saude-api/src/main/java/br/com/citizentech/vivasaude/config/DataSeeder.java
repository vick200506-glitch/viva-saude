package br.com.citizentech.vivasaude.config;

import br.com.citizentech.vivasaude.domain.Consulta;
import br.com.citizentech.vivasaude.domain.Paciente;
import br.com.citizentech.vivasaude.domain.Perfil;
import br.com.citizentech.vivasaude.domain.Prioridade;
import br.com.citizentech.vivasaude.domain.Profissional;
import br.com.citizentech.vivasaude.domain.Triagem;
import br.com.citizentech.vivasaude.domain.Usuario;
import br.com.citizentech.vivasaude.domain.Vaga;
import br.com.citizentech.vivasaude.repository.ConsultaRepository;
import br.com.citizentech.vivasaude.repository.PacienteRepository;
import br.com.citizentech.vivasaude.repository.ProfissionalRepository;
import br.com.citizentech.vivasaude.repository.TriagemRepository;
import br.com.citizentech.vivasaude.repository.UsuarioRepository;
import br.com.citizentech.vivasaude.repository.VagaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria dados FICTÍCIOS para testar e apresentar. Só roda com app.seed=true.
 * As senhas abaixo são públicas (estão no README): nunca use em produção.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final ConsultaRepository consultas;
    private final TriagemRepository triagens;
    private final VagaRepository vagas;
    private final PasswordEncoder encoder;

    public DataSeeder(UsuarioRepository usuarios, PacienteRepository pacientes, ProfissionalRepository profissionais,
                      ConsultaRepository consultas, TriagemRepository triagens, VagaRepository vagas,
                      PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.consultas = consultas;
        this.triagens = triagens;
        this.vagas = vagas;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return; // já tem dados
        }
        LocalDate hoje = LocalDate.now();

        Paciente maria = pacientes.save(new Paciente("Maria da Silva", LocalDate.of(1958, 4, 10), "•••• 4821", "(11) 99812-0001", "Centro"));
        Paciente jose = pacientes.save(new Paciente("José Pereira", LocalDate.of(1949, 9, 2), "•••• 1177", "(11) 99812-0002", "Itaquera"));
        Paciente roberto = pacientes.save(new Paciente("Roberto Machado", LocalDate.of(1962, 3, 30), "•••• 2488", "(11) 99812-0006", "Capão Redondo"));
        Paciente juliana = pacientes.save(new Paciente("Juliana Ramos", LocalDate.of(1998, 7, 12), "•••• 7031", "(11) 99812-0007", "Centro"));
        Paciente lucas = pacientes.save(new Paciente("Lucas Oliveira", LocalDate.of(1990, 1, 20), "•••• 9054", "(11) 99812-0004", "Centro"));

        Profissional ana = profissionais.save(new Profissional("Dra. Ana Souza", "Clínico geral", "UBS Centro"));
        Profissional carlos = profissionais.save(new Profissional("Dr. Carlos Lima", "Cardiologia", "AMA Especialidades"));
        Profissional rafael = profissionais.save(new Profissional("Dr. Rafael Melo", "Ortopedia", "AMA Especialidades"));
        profissionais.saveAll(List.of(
                new Profissional("Dra. Beatriz Rocha", "Pediatria", "UBS Zona Leste"),
                new Profissional("Dra. Luísa Prado", "Oftalmologia", "AMA Especialidades"),
                new Profissional("Dr. Tiago Nunes", "Odontologia", "UBS Zona Sul"),
                new Profissional("Dra. Helena Costa", "Ginecologia", "UBS Zona Leste"),
                new Profissional("Dra. Marina Alves", "Saúde mental", "AMA Especialidades"),
                new Profissional("Dr. Paulo Reis", "Dermatologia", "AMA Especialidades")));

        // ---- Usuários de teste (um por perfil, e dois pacientes para testar o isolamento) ----
        Usuario uMaria = new Usuario("maria@vivasaude.dev", encoder.encode("Paciente@123"), maria.getNome(), Perfil.PACIENTE);
        uMaria.setPaciente(maria);
        Usuario uJose = new Usuario("jose@vivasaude.dev", encoder.encode("Paciente@123"), jose.getNome(), Perfil.PACIENTE);
        uJose.setPaciente(jose);
        Usuario uAna = new Usuario("ana@vivasaude.dev", encoder.encode("Profissional@123"), ana.getNome(), Perfil.PROFISSIONAL);
        uAna.setProfissional(ana);
        Usuario uGestor = new Usuario("gestor@vivasaude.dev", encoder.encode("Gestor@123"), "Secretaria Municipal de Saúde", Perfil.GESTOR);
        usuarios.saveAll(List.of(uMaria, uJose, uAna, uGestor));

        // ---- Consultas ----
        Consulta c1 = new Consulta(maria, "Cardiologia", Prioridade.AMARELO, "Triagem");
        c1.definirCriadaEm(hoje.minusDays(12));
        c1.agendar(carlos, hoje.plusDays(18), LocalTime.of(9, 0));

        Consulta c2 = new Consulta(maria, "Clínico geral", Prioridade.VERDE, "Aplicativo");
        c2.definirCriadaEm(hoje.minusDays(27));
        c2.agendar(ana, hoje.minusDays(20), LocalTime.of(14, 30));
        c2.registrarComparecimento(true);

        Consulta c3 = new Consulta(jose, "Cardiologia", Prioridade.AMARELO, "UBS");
        c3.definirCriadaEm(hoje.minusDays(20));
        c3.agendar(carlos, hoje.plusDays(5), LocalTime.of(10, 0));

        Consulta c4 = new Consulta(roberto, "Cardiologia", Prioridade.AMARELO, "Triagem");
        c4.definirCriadaEm(hoje.minusDays(9));

        Consulta c5 = new Consulta(juliana, "Saúde mental", Prioridade.AMARELO, "UBS");
        c5.definirCriadaEm(hoje.minusDays(6));

        Consulta c6 = new Consulta(lucas, "Ortopedia", Prioridade.VERDE, "Aplicativo");
        c6.definirCriadaEm(hoje.minusDays(10));
        c6.agendar(rafael, hoje.plusDays(2), LocalTime.of(8, 30));

        Consulta c7 = new Consulta(jose, "Clínico geral", Prioridade.VERDE, "Aplicativo");
        c7.definirCriadaEm(hoje.minusDays(12));
        c7.agendar(ana, hoje.minusDays(5), LocalTime.of(10, 0));
        c7.cancelar();

        // Juliana cancelou uma cardiologia daqui a 3 dias: o horário virou vaga
        Consulta c8 = new Consulta(juliana, "Cardiologia", Prioridade.VERDE, "UBS");
        c8.definirCriadaEm(hoje.minusDays(18));
        c8.agendar(carlos, hoje.plusDays(3), LocalTime.of(15, 0));
        c8.cancelar();

        consultas.saveAll(List.of(c1, c2, c3, c4, c5, c6, c7, c8));
        vagas.save(new Vaga(carlos, "Cardiologia", hoje.plusDays(3), LocalTime.of(15, 0), "Cancelamento"));

        // ---- Triagens aguardando avaliação ----
        triagens.saveAll(List.of(
                new Triagem(juliana, "Mente e emoções", "Saúde mental",
                        List.of("Tristeza ou ansiedade que não passa", "Dificuldade para dormir"),
                        List.of(), "Mais de 1 mês", "Ando sem vontade de sair de casa."),
                new Triagem(lucas, "Pele", "Dermatologia",
                        List.of("Mancha ou pinta que mudou"), List.of(), "1 a 4 semanas", null)));
    }
}
