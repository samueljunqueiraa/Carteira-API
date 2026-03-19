# CARTEIRA-API

API REST para gerenciamento de carteiras digitais, com foco em transferências entre usuários e consulta de extrato.

Construída com **Java 21** e **Spring Boot 3**, este projeto demonstra boas práticas de desenvolvimento backend: modelagem de domínio, validação de regras de negócio, testes automatizados e containerização.

---

## O que o sistema faz

Um usuário se cadastra com seu CPF e recebe uma carteira digital. A partir daí, ele pode:

- **Transferir saldo** para outro usuário (identificado pelo CPF do destinatário)
- **Consultar o extrato** da sua carteira, com histórico paginado de transações

Regras de negócio aplicadas:

- CPF deve ser válido (dígitos verificadores) e único no sistema
- Saldo não pode ficar negativo após uma transferência
- Não é permitido transferir para a própria carteira
- O valor da transferência deve ser positivo e com no máximo 2 casas decimais

---

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 |
| Banco de dados | PostgreSQL 16 |
| Migrações | Flyway |
| Testes de integração | Testcontainers |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Containerização | Docker + Docker Compose |

---

## Como rodar localmente

**Pré-requisitos:** Docker e Docker Compose instalados.

```bash
# 1. Clone o repositório
git clone https://github.com/samueljunqueiraa/Carteira-API.git

# 2. Suba o banco de dados
docker compose up -d postgres

# 3. Rode a aplicação
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

A documentação interativa (Swagger UI) estará em `http://localhost:8080/swagger-ui.html`.

---

## Endpoints principais

### Carteiras

```
POST   /api/v1/wallets          Cria uma nova carteira (requer CPF e nome)
GET    /api/v1/wallets/{cpf}    Consulta saldo e dados de uma carteira
```

### Transações

```
POST   /api/v1/transactions/transfer    Realiza uma transferência entre carteiras
GET    /api/v1/wallets/{cpf}/statement  Consulta extrato paginado
```

#### Exemplo — criar carteira

```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "cpf": "123.456.789-09"
  }'
```

#### Exemplo — transferir saldo

```bash
curl -X POST http://localhost:8080/api/v1/transactions/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCpf": "123.456.789-09",
    "targetCpf": "987.654.321-00",
    "amount": 150.00
  }'
```

#### Exemplo — consultar extrato

```bash
curl "http://localhost:8080/api/v1/wallets/123.456.789-09/statement?page=0&size=10"
```

---

## Estrutura do projeto

```
src/
├── main/java/com/carteira/
│   ├── controller/       # Controladores REST e DTOs de request/response
│   ├── service/          # Regras de negócio e orquestração
│   ├── domain/           # Entidades e exceções de domínio
│   ├── repository/       # Interfaces Spring Data JPA
│   └── config/           # Configurações (OpenAPI, tratamento de erros)
└── test/
    ├── unit/             # Testes de regras de negócio (sem I/O)
    └── integration/      # Testes com banco real via Testcontainers
```

---

## Decisões técnicas

**Por que `BigDecimal` para valores monetários?**
`double` e `float` têm erros de precisão de ponto flutuante. `0.1 + 0.2` resulta em `0.30000000000000004`. Para dinheiro, isso é inaceitável. `BigDecimal` garante aritmética exata, e o banco armazena o valor como `NUMERIC(19,2)`.

**Por que Flyway para migrações?**
Scripts SQL versionados garantem que qualquer pessoa que clonar o repositório terá exatamente o mesmo schema do banco, sem precisar rodar scripts manuais. O histórico de alterações do schema fica junto ao código.

**Por que Testcontainers em vez de H2?**
Bancos em memória como H2 têm comportamento diferente do PostgreSQL em casos como tipos de dados, constraints e comportamento de transações. Testcontainers sobe um PostgreSQL real em um container Docker durante os testes, eliminando essa diferença.

**Tratamento de erros centralizado**
Um `@ControllerAdvice` global captura exceções de domínio (ex: `InsufficientBalanceException`, `WalletNotFoundException`) e retorna respostas HTTP padronizadas com código de status e mensagem apropriados, evitando stack traces expostos ao cliente.

---

## Rodando os testes

```bash
# Todos os testes (requer Docker rodando para os de integração)
./mvnw test

# Apenas testes unitários
./mvnw test -Dgroups="unit"

# Apenas testes de integração
./mvnw test -Dgroups="integration"
```

---

## Possíveis evoluções

- Autenticação com Spring Security + JWT
- Depósito via integração com gateway de pagamento (mock)
- Notificações assíncronas por e-mail ao receber uma transferência
- Rate limiting por CPF para evitar abuso

---

## Autor

Feito por **Samuel Junqueira** — [LinkedIn](https://linkedin.com/in/seu-perfil](https://www.linkedin.com/in/samuel-junqueira/)) · [GitHub](https://github.com/samueljunqueiraa)
