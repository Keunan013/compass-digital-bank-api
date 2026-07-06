# Digital Bank API

API REST de um banco digital simplificado: cadastro e consulta de contas, transferência de
valores entre contas e consulta de movimentações.

Stack: Java 21, Spring Boot 3.3, PostgreSQL, RabbitMQ e Maven.

## Como rodar

Pré-requisito: Docker.

```bash
docker compose up --build
```

Para subir o Postgres, o RabbitMQ e a API. Depois:

- Swagger: http://localhost:8080/swagger-ui.html
- Painel do RabbitMQ: http://localhost:15672

Quando subir pela primeira vez são criados alguns usuários de teste (senha `Password123!`):

- `user1@example.com` e `user2@example.com` (comuns)
- `admin@example.com` (admin)

No Swagger, faça `login`, copie o `accessToken`, clique em **Authorize** e cole o token para
testar os endpoints protegidos.

Para rodar o app fora do container (subindo só a infra no Docker):

```bash
docker compose up -d postgres rabbitmq
./mvnw spring-boot:run
```

O schema é criado automaticamente pelo Flyway na subida. As configurações ficam em variáveis de
ambiente — `.env.example`.

## Endpoints

Os endpoints de `auth` são abertos; os demais exigem o header `Authorization: Bearer <token>`.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/auth/register` | Cadastra um usuário e devolve o token |
| POST | `/api/v1/auth/login` | Autentica e devolve o token |
| POST | `/api/v1/accounts` | Abre uma conta do usuário logado |
| GET | `/api/v1/accounts` | Lista as contas do usuário |
| GET | `/api/v1/accounts/{id}` | Detalhe de uma conta |
| POST | `/api/v1/transfers` | Transfere entre contas (header opcional `Idempotency-Key`) |
| GET | `/api/v1/accounts/{id}/transactions` | Extrato da conta |

## Testes

```bash
mvn test     # unitários
mvn verify   # unitários + integração (sobe Postgres e RabbitMQ com Testcontainers; precisa de Docker)
```

## Decisões de arquitetura

**Organização em camadas (hexagonal).** O domínio (`domain`) propositalmente não possuí aclopamento com o framework, e
declara interfaces (ports) do que precisa. A aplicação (`application`) implementa os casos de uso,
e os adapters (`adapter.in.web`, `adapter.out.*`) ligam isso ao mundo exterior (HTTP, banco,
broker), a fim de manter regras de negócio isoladas.

**Transferência e concorrência.** A transferência roda dentro de uma transação e trava as duas
contas com lock pessimista (`SELECT ... FOR UPDATE`), sempre na mesma ordem de id para evitar
deadlock entre transferências opostas. Há também a coluna de versão (`@Version`) como reforço. O
header `Idempotency-Key` (opcional) evita débito duplicado em caso de reenvio. Existe um teste que
dispara 100 transferências paralelas contra um Postgres real para garantir que não há saldo
perdido nem negativo.

**Notificação.** Depois que a transferência é confirmada, um evento é publicado numa fila do
RabbitMQ e um consumer entrega a notificação (aqui, via log — a interface permite trocar por
e-mail/SMS depois). A fila tem retry e uma dead-letter queue para mensagens que continuam
falhando. Publicar só após o commit para garantir que uma transferência revertida não gera notificação.

**Segurança.** Autenticação via JWT (stateless) com senha em BCrypt, autorização por posse da
conta, rate limiting por cliente (configurável) e validação dos inputs. Erros seguem um formato
JSON padronizado.

**Banco e migrations.** PostgreSQL com migrations versionadas em Flyway.
