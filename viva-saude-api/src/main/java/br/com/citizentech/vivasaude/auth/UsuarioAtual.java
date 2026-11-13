package br.com.citizentech.vivasaude.auth;

import br.com.citizentech.vivasaude.domain.Paciente;
import br.com.citizentech.vivasaude.domain.Usuario;
import br.com.citizentech.vivasaude.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Descobre QUEM está chamando a API a partir do token.
 * Regra de ouro: o id do paciente vem daqui, nunca da URL ou do corpo do pedido.
 */
@Component
public class UsuarioAtual {

    private final UsuarioRepository usuarios;

    public UsuarioAtual(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    public Usuario de(Jwt jwt) {
        return usuarios.findById(Long.valueOf(jwt.getSubject()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não encontrado."));
    }

    public Paciente paciente(Jwt jwt) {
        Paciente paciente = de(jwt).getPaciente();
        if (paciente == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este usuário não tem cadastro de paciente.");
        }
        return paciente;
    }
}
