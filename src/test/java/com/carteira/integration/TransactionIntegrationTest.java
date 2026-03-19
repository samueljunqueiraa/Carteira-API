package com.carteira.integration;

import com.carteira.domain.Wallet;
import com.carteira.repository.TransactionRepository;
import com.carteira.repository.WalletRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

class TransactionIntegrationTest extends AbstractIntegrationTest {

    private static final String SOURCE_CPF = "529.982.247-25";
    private static final String TARGET_CPF = "111.444.777-35";

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
    }

    private void createWallet(String name, String cpf) {
        given().contentType(ContentType.JSON)
               .body(Map.of("name", name, "cpf", cpf))
               .when().post("/wallets");
    }

    private void seedBalance(String cpf, BigDecimal amount) {
        Wallet wallet = walletRepository.findByCpf(cpf).orElseThrow();
        wallet.credit(amount);
        walletRepository.save(wallet);
    }

    @Test
    void transfer_whenValidRequest_shouldReturn201AndUpdateBalances() {
        createWallet("João Silva", SOURCE_CPF);
        createWallet("Maria Souza", TARGET_CPF);
        seedBalance(SOURCE_CPF, new BigDecimal("200.00"));

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("sourceCpf", SOURCE_CPF, "targetCpf", TARGET_CPF, "amount", "150.00"))
        .when()
            .post("/transactions/transfer")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("sourceCpf", equalTo(SOURCE_CPF))
            .body("targetCpf", equalTo(TARGET_CPF))
            .body("amount", equalTo(new BigDecimal("150.00")));

        given().when().get("/wallets/" + SOURCE_CPF).then()
            .body("balance", equalTo(new BigDecimal("50.00")));

        given().when().get("/wallets/" + TARGET_CPF).then()
            .body("balance", equalTo(new BigDecimal("150.00")));
    }

    @Test
    void transfer_whenSameCpf_shouldReturn422() {
        createWallet("João Silva", SOURCE_CPF);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("sourceCpf", SOURCE_CPF, "targetCpf", SOURCE_CPF, "amount", "10.00"))
        .when()
            .post("/transactions/transfer")
        .then()
            .statusCode(422);
    }

    @Test
    void transfer_whenInsufficientBalance_shouldReturn422() {
        createWallet("João Silva", SOURCE_CPF);
        createWallet("Maria Souza", TARGET_CPF);
        // saldo inicial é 0.00 — qualquer valor > 0 deve resultar em 422

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("sourceCpf", SOURCE_CPF, "targetCpf", TARGET_CPF, "amount", "50.00"))
        .when()
            .post("/transactions/transfer")
        .then()
            .statusCode(422)
            .body("status", equalTo(422));
    }

    @Test
    void transfer_whenSourceWalletNotFound_shouldReturn404() {
        createWallet("Maria Souza", TARGET_CPF);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("sourceCpf", SOURCE_CPF, "targetCpf", TARGET_CPF, "amount", "50.00"))
        .when()
            .post("/transactions/transfer")
        .then()
            .statusCode(404)
            .body("message", notNullValue());
    }

    @Test
    void getStatement_whenWalletHasTransactions_shouldReturnPaginatedResults() {
        createWallet("João Silva", SOURCE_CPF);
        createWallet("Maria Souza", TARGET_CPF);
        seedBalance(SOURCE_CPF, new BigDecimal("300.00"));

        given().contentType(ContentType.JSON)
               .body(Map.of("sourceCpf", SOURCE_CPF, "targetCpf", TARGET_CPF, "amount", "100.00"))
               .when().post("/transactions/transfer");

        given().contentType(ContentType.JSON)
               .body(Map.of("sourceCpf", SOURCE_CPF, "targetCpf", TARGET_CPF, "amount", "50.00"))
               .when().post("/transactions/transfer");

        given()
        .when()
            .get("/wallets/" + SOURCE_CPF + "/statement?page=0&size=10")
        .then()
            .statusCode(200)
            .body("content", hasSize(2))
            .body("totalElements", equalTo(new BigDecimal("2")))
            .body("page", equalTo(new BigDecimal("0")));
    }
}
