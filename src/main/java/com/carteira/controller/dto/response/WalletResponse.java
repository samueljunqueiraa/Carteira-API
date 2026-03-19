package com.carteira.controller.dto.response;

import com.carteira.domain.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class WalletResponse {

    private UUID id;
    private String name;
    private String cpf;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    private WalletResponse() {
    }

    public static WalletResponse from(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.id = wallet.getId();
        response.name = wallet.getName();
        response.cpf = wallet.getCpf();
        response.balance = wallet.getBalance();
        response.createdAt = wallet.getCreatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCpf() {
        return cpf;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
