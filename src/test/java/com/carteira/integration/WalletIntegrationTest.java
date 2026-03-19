package com.carteira.integration;

import com.carteira.repository.TransactionRepository;
import com.carteira.repository.WalletRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

class WalletIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    @BeforeEach
    void cleanDatabase() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    void createWallet_whenValidRequest_shouldReturn201WithWalletData() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "João Silva", "cpf", "529.982.247-25"))
        .when()
            .post("/wallets")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("João Silva"))
            .body("cpf", equalTo("529.982.247-25"))
            .body("balance", equalTo(new java.math.BigDecimal("0.00")));
    }

    @Test
    void createWallet_whenCpfHasInvalidFormat_shouldReturn400() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "João Silva", "cpf", "12345678900"))
        .when()
            .post("/wallets")
        .then()
            .statusCode(400)
            .body("status", equalTo(400));
    }

    @Test
    void createWallet_whenCpfHasInvalidCheckDigits_shouldReturn400() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("name", "João Silva", "cpf", "529.982.247-00"))
        .when()
            .post("/wallets")
        .then()
            .statusCode(400)
            .body("message", containsString("CPF inválido"));
    }

    @Test
    void createWallet_whenCpfAlreadyRegistered_shouldReturn409() {
        Map<String, String> request = Map.of("name", "João Silva", "cpf", "529.982.247-25");

        given().contentType(ContentType.JSON).body(request).when().post("/wallets");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/wallets")
        .then()
            .statusCode(409)
            .body("message", containsString("529.982.247-25"));
    }

    @Test
    void findByCpf_whenWalletExists_shouldReturn200WithWalletData() {
        given().contentType(ContentType.JSON)
               .body(Map.of("name", "João Silva", "cpf", "529.982.247-25"))
               .when().post("/wallets");

        given()
        .when()
            .get("/wallets/529.982.247-25")
        .then()
            .statusCode(200)
            .body("cpf", equalTo("529.982.247-25"))
            .body("name", equalTo("João Silva"))
            .body("balance", notNullValue());
    }

    @Test
    void findByCpf_whenWalletDoesNotExist_shouldReturn404() {
        given()
        .when()
            .get("/wallets/529.982.247-25")
        .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("message", containsString("529.982.247-25"));
    }
}