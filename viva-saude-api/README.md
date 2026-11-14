# Viva Saúde — API (Spring Boot)

API do MVP do Viva Saúde (Global Solution · CitizenTech) com **login e senha** e **três perfis**, cada um com o seu painel:

| Perfil | Painel | Prefixo das rotas |
|---|---|---|
| `PACIENTE` | App do cidadão | `/api/paciente/**` |
| `PROFISSIONAL` | Painel dos profissionais | `/api/profissional/**` |
| `GESTOR` | Painel da prefeitura | `/api/prefeitura/**` |

Stack: Java 17, Spring Boot 3.5.15, Spring Security, JWT, JPA/Hibernate, H2 (dev) e PostgreSQL.

---

## 1. Como rodar

Pré-requisitos: **JDK 17+** e **Maven** (ou abra a pasta no IntelliJ, que já traz o Maven).

```bash
mvn spring-boot:run
```

A API sobe em `http://localhost:8080` usando um banco **H2 em memória**, com dados fictícios. Os dados somem quando você para a API.

Para rodar os testes de segurança:

```bash
mvn test
```

> Dica: no IntelliJ, você pode gerar o Maven Wrapper (`mvnw`) com *Maven → Execute Maven Goal → `wrapper:wrapper`*. Assim o grupo inteiro usa a mesma versão.

## 2. Usuários de teste

| Perfil | E-mail | Senha |
|---|---|---|
| Paciente | `maria@vivasaude.dev` | `Paciente@123` |
| Paciente | `jose@vivasaude.dev` | `Paciente@123` |
| Profissional | `ana@vivasaude.dev` | `Profissional@123` |
| Gestor (prefeitura) | `gestor@vivasaude.dev` | `Gestor@123` |

Essas senhas são públicas (estão aqui). Por isso o perfil `postgres` **não** cria esses usuários por padrão.

## 3. Testando com curl (ou Postman/Insomnia)

```bash
# 1) Login: devolve o token e o perfil
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@vivasaude.dev","senha":"Paciente@123"}'

# 2) Copie o "token" da resposta e use em todo pedido seguinte
TOKEN="cole-o-token-aqui"
curl -s http://localhost:8080/api/paciente/consultas -H "Authorization: Bearer $TOKEN"

# 3) Tente entrar no painel da prefeitura com o token da Maria → 403
curl -i http://localhost:8080/api/prefeitura/indicadores -H "Authorization: Bearer $TOKEN"
```

## 4. Rotas

### Autenticação (públicas)
| Método | Rota | O que faz |
|---|---|---|
| POST | `/auth/login` | `{email, senha}` → `{token, perfil, nome, expiraEm}` |
| POST | `/auth/cadastro` | Cria conta de **paciente** e já devolve o token |
| GET | `/auth/me` | Quem está logado (precisa de token) |

### Paciente (`PACIENTE`)
| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/paciente/me` | Meus dados |
| GET | `/api/paciente/consultas` | Minhas consultas |
| POST | `/api/paciente/consultas` | Pedir consulta `{especialidade}` |
| PATCH | `/api/paciente/consultas/{id}/cancelar` | Cancelar **uma consulta minha** |
| GET | `/api/paciente/triagens` | Minhas triagens |
| POST | `/api/paciente/triagens` | Enviar triagem `{area, especialidade, sintomas[], alertas[], duracao, relato}` |
| GET | `/api/paciente/vagas-oferecidas` | Vagas oferecidas para mim |
| POST | `/api/paciente/vagas/{id}/aceitar` | Aceitar uma vaga oferecida para mim |
| POST | `/api/paciente/vagas/{id}/recusar` | Recusar uma vaga oferecida para mim |
| GET / POST | `/api/paciente/exames` | Ver meus exames / pedir exame `{nome, unidade}` |
| GET / POST | `/api/paciente/acompanhamentos` | Ver / registrar medida `{tipo: PRESSAO_ARTERIAL, GLICEMIA ou PESO, valor}` |
| GET | `/api/paciente/historico` | Meu histórico (consultas, exames e triagens) |

### Profissional (`PROFISSIONAL`)
| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/profissional/triagens` | Triagens aguardando (mais urgentes primeiro) |
| POST | `/api/profissional/triagens/{id}/avaliacao` | `{classificacao: VERMELHO/AMARELO/VERDE, encaminhar: true/false}` |
| GET | `/api/profissional/solicitacoes` | Pedidos aguardando agendamento |
| POST | `/api/profissional/consultas/{id}/agendar` | `{data: "2026-12-01", horario: "09:30"}` |
| GET | `/api/profissional/agenda` | Consultas agendadas |
| PATCH | `/api/profissional/consultas/{id}/comparecimento` | `{compareceu: true/false}` |
| PATCH | `/api/profissional/consultas/{id}/cancelar` | Cancelar pela unidade |
| GET | `/api/profissional/pacientes` | Lista de pacientes |
| GET | `/api/profissional/vagas` | Vagas abertas e reaproveitadas |
| GET | `/api/profissional/vagas/{id}/elegiveis` | Quem pode ficar com a vaga, em ordem |
| POST | `/api/profissional/vagas/{id}/oferecer` | `{consultaId}` oferece a vaga a um pedido |
| POST | `/api/profissional/vagas/{id}/resposta` | `{aceitou: true/false}` registra a resposta do paciente |
| GET | `/api/profissional/exames` | Exames para agendar e para registrar resultado |
| POST | `/api/profissional/exames/{id}/agendar` | `{data, unidade}` |
| POST | `/api/profissional/exames/{id}/resultado` | `{resultado}` |
| GET | `/api/profissional/pacientes/{id}/historico` | Histórico de um paciente |
| GET | `/api/profissional/pacientes/{id}/acompanhamentos` | Medidas de um paciente |

### Prefeitura (`GESTOR`)
| Método | Rota | O que faz |
|---|---|---|
| GET | `/api/prefeitura/indicadores` | Só números agregados, **sem dados pessoais** |

Especialidades aceitas: Cardiologia, Dermatologia, Oftalmologia, Pediatria, Ginecologia, Ortopedia, Saúde mental, Odontologia, Clínico geral.

## 5. Como a segurança funciona (para explicar na apresentação)

1. **Senha nunca é salva.** O banco guarda só o *hash* BCrypt (`senhaHash`). Nem quem tem acesso ao banco consegue ver as senhas.
2. **Login gera um token JWT.** Ele é assinado com uma chave secreta que só o servidor conhece. Se alguém alterar o token (por exemplo, trocar `PACIENTE` por `GESTOR`), a assinatura deixa de bater e a API recusa (`401`).
3. **O token carrega só o id e o perfil**, nunca dados de saúde. Atenção: o conteúdo de um JWT pode ser lido por qualquer um, ele é assinado mas não é criptografado.
4. **Regra por perfil** em `SecurityConfig`: `/api/paciente/**` só para `PACIENTE`, e assim por diante. Perfil errado recebe `403`.
5. **Isolamento entre pacientes:** o id do paciente vem **do token**, nunca da URL. A Maria não consegue ver nem cancelar consultas do José. Se tentar, recebe `404`, como se a consulta nem existisse.
6. **A prefeitura vê só números agregados** (princípio da minimização de dados da LGPD).
7. **O cadastro público sempre cria um paciente.** Não existe campo "perfil" no cadastro, então ninguém se cadastra como gestor.
8. **Respostas usam DTOs** (`record`), nunca a entidade direto. Assim o `senhaHash` nunca sai em um JSON por acidente.

Os testes em `src/test/.../SegurancaTest.java` provam cada um desses pontos.

## 6. Rodando tudo com Docker (PostgreSQL + API + site)

Na pasta **raiz do repositório** (`viva-saude`, onde está o `docker-compose.yml`):

```bash
cp .env.example .env          # só na primeira vez; depois edite as senhas no .env
docker compose up --build     # constrói a API e sobe banco, API e site
```

- Site: http://localhost:5500
- API: http://localhost:8080
- Banco: PostgreSQL, sem porta aberta no seu Mac (só a API acessa)

Os dados ficam no volume `dados-postgres` e **não somem** ao desligar.

| Comando | O que faz |
|---|---|
| `docker compose up --build` | Sobe tudo (reconstrói a API se o código mudou) |
| `docker compose up -d` | Sobe em segundo plano |
| `docker compose logs -f api` | Mostra os logs da API |
| `docker compose down` | Desliga tudo (os dados continuam no volume) |
| `docker compose down -v` | Desliga e **apaga os dados** (volta aos dados de exemplo) |
| `docker compose exec db psql -U vivasaude -d vivasaude` | Abre o banco para consultas SQL |

**Nunca** coloque o `.env` no GitHub: ele guarda as senhas.

## 7. Ligando com o front-end

O navegador só deixa o front chamar a API se o endereço dele estiver liberado no CORS (`app.cors.origens` em `application.yml`). Já estão liberados `http://localhost:5500` (Live Server do VS Code) e `http://localhost:3000` (React).

Fluxo no front:
1. Tela de login → `POST /auth/login`.
2. Guarde o `token` e abra o painel de acordo com o `perfil`.
3. Em todo `fetch`, envie o cabeçalho `Authorization: Bearer <token>`.
4. Se a API responder `401`, o token venceu: volte para a tela de login.

## 8. Próximos passos (antes de produção de verdade)

- Limitar tentativas de login (contra quem tenta adivinhar senhas).
- HTTPS obrigatório.
- Registrar quem acessou os dados de cada paciente (auditoria, exigência da LGPD para dados de saúde).
- Restringir o profissional aos pacientes da sua unidade.
- Migrações de banco com Flyway em vez de `ddl-auto: update`.
- Entidades que faltam do modelo do projeto: `responsaveis`, `unidades` e `notificacoes`. O histórico não é tabela: é montado na hora a partir de consultas, exames e triagens.
