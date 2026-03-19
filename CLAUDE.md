# CLAUDE.md — carteira-api

Este arquivo é lido automaticamente pelo Claude Code ao iniciar neste projeto.
Siga todas as instruções abaixo em qualquer tarefa que executar.

---

## Visão geral do projeto

API REST de carteira digital construída com Java 21 + Spring Boot 3.3 + PostgreSQL 16.

O sistema permite:
- Transferências de saldo entre carteiras
- Consulta de extrato paginado por carteira

Usuários são identificados por CPF. Não há autenticação nesta versão — isso é
um projeto de portfólio com escopo deliberadamente limitado.

---

## Regras de negócio — nunca viole estas regras

- CPF deve ser válido (algoritmo dos dígitos verificadores) e único no sistema
- Saldo nunca pode ficar negativo após uma transferência
- Não é permitido transferir para a própria carteira (sourceCpf == targetCpf)
- O valor da transferência deve ser maior que zero
- Valores monetários têm no máximo 2 casas decimais
- NUNCA use `double` ou `float` para dinheiro — sempre `BigDecimal`
- O banco armazena valores monetários como `NUMERIC(19,2)`

---

## Stack obrigatória

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3 |
| Banco de dados | PostgreSQL 16 |
| Persistência | Spring Data JPA |
| Migrações | Flyway |
| Documentação | SpringDoc OpenAPI |
| Testes unitários | JUnit 5 + Mockito |
| Testes de integração | Testcontainers + RestAssured |
| Containerização | Docker + Docker Compose |

### Proibições de stack

- Não use H2 ou qualquer banco em memória — nem para testes
- Não use Lombok — código explícito é mais didático para portfólio
- Não use arquitetura hexagonal ou DDD — estrutura de pacotes simples
- Não adicione dependências fora da lista acima sem perguntar primeiro

---

## Estrutura de pacotes

```
src/main/java/com/carteira/
├── controller/
│   ├── WalletController.java
│   ├── TransactionController.java
│   └── dto/                        # request e response separados por subpacote
│       ├── request/
│       └── response/
├── service/
│   ├── WalletService.java
│   └── TransactionService.java
├── domain/
│   ├── Wallet.java                 # entidade JPA
│   ├── Transaction.java            # entidade JPA
│   └── exception/
│       ├── WalletNotFoundException.java
│       ├── InsufficientBalanceException.java
│       ├── SameWalletTransferException.java
│       └── CpfAlreadyExistsException.java
├── repository/
│   ├── WalletRepository.java
│   └── TransactionRepository.java
└── config/
    ├── OpenApiConfig.java
    └── GlobalExceptionHandler.java
```

---

## Endpoints da API

### Carteiras

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/wallets` | Cria uma nova carteira |
| GET | `/api/v1/wallets/{cpf}` | Consulta saldo e dados |

### Transações

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/transactions/transfer` | Transfere saldo entre carteiras |
| GET | `/api/v1/wallets/{cpf}/statement` | Extrato paginado (`?page=0&size=10`) |

---

## Schema do banco de dados

### Tabela `wallets`

```sql
CREATE TABLE wallets (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(100) NOT NULL,
    cpf        VARCHAR(14)  NOT NULL UNIQUE,
    balance    NUMERIC(19,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);
```

### Tabela `transactions`

```sql
CREATE TABLE transactions (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    source_wallet_id UUID      NOT NULL REFERENCES wallets(id),
    target_wallet_id UUID      NOT NULL REFERENCES wallets(id),
    amount         NUMERIC(19,2) NOT NULL,
    created_at     TIMESTAMP  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_amount_positive CHECK (amount > 0),
    CONSTRAINT chk_different_wallets CHECK (source_wallet_id != target_wallet_id)
);
```

### Migrações Flyway (ordem obrigatória)

```
src/main/resources/db/migration/
├── V1__create_wallets_table.sql
└── V2__create_transactions_table.sql
```

---

## Padrão de resposta de erro

Todos os erros devem seguir este formato JSON:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "CPF inválido: 000.000.000-00",
  "timestamp": "2025-03-19T14:30:00"
}
```

O `GlobalExceptionHandler` deve mapear cada exceção de domínio ao HTTP status correto:

| Exceção | HTTP Status |
|---|---|
| `WalletNotFoundException` | 404 Not Found |
| `InsufficientBalanceException` | 422 Unprocessable Entity |
| `SameWalletTransferException` | 422 Unprocessable Entity |
| `CpfAlreadyExistsException` | 409 Conflict |
| `MethodArgumentNotValidException` | 400 Bad Request |

---

## Regras de código

- Métodos de serviço que alteram estado devem ter `@Transactional`
- Validações de entrada (campos obrigatórios, formatos) ficam nos DTOs com Bean Validation (`@NotBlank`, `@NotNull`, `@DecimalMin`)
- Validações de regra de negócio (saldo insuficiente, CPF duplicado) ficam no `Service`
- Nunca retorne entidades JPA diretamente nos controllers — sempre use DTOs de response
- CPF é recebido e exibido no formato `000.000.000-00`

---

## Regras de teste

### Testes unitários (`src/test/.../service/`)
- Testam apenas os services com Mockito — sem Spring context, sem banco
- Nomenclatura: `metodo_cenario_resultadoEsperado`
    - Exemplo: `transfer_whenInsufficientBalance_shouldThrowException`
- Cobrir todos os caminhos de erro das regras de negócio

### Testes de integração (`src/test/.../integration/`)
- Usam `@SpringBootTest` + Testcontainers com PostgreSQL real
- Usam RestAssured para chamadas HTTP
- Testam o fluxo completo: request HTTP → banco → response
- Um `@BeforeEach` deve limpar as tabelas entre os testes

---

## Docker Compose

O `docker-compose.yml` deve conter apenas o serviço PostgreSQL.
A aplicação roda localmente via `./mvnw spring-boot:run`.

```yaml
# variáveis esperadas no application.yml
spring.datasource.url=jdbc:postgresql://localhost:5432/carteira
spring.datasource.username=carteira
spring.datasource.password=carteira
```

---

## Convenção de commits

Use commits semânticos ao final de cada etapa concluída:

```
feat: cria entidades Wallet e Transaction
feat: implementa endpoint de transferência
feat: adiciona extrato paginado
test: adiciona testes unitários de TransactionService
test: adiciona testes de integração dos endpoints
chore: configura docker-compose e flyway
docs: adiciona README e exemplos de uso
```

---

## Ordem de construção sugerida

Siga esta ordem ao implementar o projeto do zero:

1. `pom.xml` com todas as dependências
2. `docker-compose.yml`
3. `application.yml`
4. Migrations Flyway (V1 e V2)
5. Entidades JPA (`Wallet`, `Transaction`)
6. Repositórios Spring Data JPA
7. Exceções de domínio
8. Services com regras de negócio
9. DTOs de request e response
10. Controllers REST
11. `GlobalExceptionHandler`
12. `OpenApiConfig`
13. Testes unitários dos services
14. Testes de integração dos endpoints

**Antes de iniciar qualquer etapa, informe o que será feito e aguarde confirmação.**