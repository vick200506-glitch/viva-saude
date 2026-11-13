package br.com.citizentech.vivasaude.auth;

import br.com.citizentech.vivasaude.api.dto.CadastroRequest;
import br.com.citizentech.vivasaude.api.dto.LoginRequest;
import br.com.citizentech.vivasaude.api.dto.LoginResponse;
import br.com.citizentech.vivasaude.api.dto.UsuarioResponse;
import br.com.citizentech.vivasaude.domain.Paciente;
import br.com.citizentech.vivasaude.domain.Perfil;
import br.com.citizentech.vivasaude.domain.Usuario;
import br.com.citizentech.vivasaude.repository.PacienteRepository;
import br.com.citizentech.vivasaude.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UsuarioAtual usuarioAtual;

    public AuthController(UsuarioRepository usuarios, PacienteRepository pacientes, PasswordEncoder passwordEncoder,
                          TokenService tokenService, UsuarioAtual usuarioAtual) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.usuarioAtual = usuarioAtual;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        // Mesma mensagem para "e-mail não existe" e "senha errada":
        // assim ninguém descobre quais e-mails têm conta.
        Usuario usuario = usuarios.findByEmail(normalizar(req.email()))
                .filter(u -> passwordEncoder.matches(req.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos."));
        return resposta(usuario);
    }

    /** Cadastro público de PACIENTE. Profissionais e gestores são criados pela prefeitura, não por aqui. */
    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public LoginResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        String email = normalizar(req.email());
        if (usuarios.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado.");
        }
        Paciente paciente = pacientes.save(new Paciente(req.nome().trim(), req.nascimento(), req.cartaoSus(),
                req.telefone(), req.bairro()));
        Usuario usuario = new Usuario(email, passwordEncoder.encode(req.senha()), paciente.getNome(), Perfil.PACIENTE);
        usuario.setPaciente(paciente);
        usuarios.save(usuario);
        return resposta(usuario);
    }

    /** Quem sou eu? O front chama isto ao abrir, para saber se o token ainda vale. */
    @GetMapping("/me")
    public UsuarioResponse me(@AuthenticationPrincipal Jwt jwt) {
        return UsuarioResponse.from(usuarioAtual.de(jwt));
    }

    private LoginResponse resposta(Usuario usuario) {
        TokenService.Token token = tokenService.gerar(usuario);
        return new LoginResponse(token.valor(), usuario.getPerfil(), usuario.getNome(), token.expiraEm());
    }

    private static String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
