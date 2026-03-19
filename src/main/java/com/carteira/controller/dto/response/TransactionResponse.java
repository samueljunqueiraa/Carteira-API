package com.carteira.controller.dto.response;

import com.carteira.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionResponse {

    private UUID id;
    private String sourceCpf;
    private String targetCpf;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    private TransactionResponse() {
    }

    public static TransactionResponse from(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.id = transaction.getId();
        response.sourceCpf = transaction.getSourceWallet().getCpf();
        response.targetCpf = transaction.getTargetWallet().getCpf();
        response.amount = transaction.getAmount();
        response.createdAt = transaction.getCreatedAt();
        return response;
    }

    public UUID getId() {
        return id;
    }

    public String getSourceCpf() {
        return sourceCpf;
    }

    public String getTargetCpf() {
        return targetCpf;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
