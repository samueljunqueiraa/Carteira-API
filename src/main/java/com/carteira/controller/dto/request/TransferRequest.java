package com.carteira.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class TransferRequest {

    @NotBlank(message = "O CPF de origem é obrigatório.")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF de origem deve estar no formato 000.000.000-00.")
    private String sourceCpf;

    @NotBlank(message = "O CPF de destino é obrigatório.")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF de destino deve estar no formato 000.000.000-00.")
    private String targetCpf;

    @NotNull(message = "O valor é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da transferência deve ser maior que zero.")
    private BigDecimal amount;

    public TransferRequest() {
    }

    public TransferRequest(String sourceCpf, String targetCpf, BigDecimal amount) {
        this.sourceCpf = sourceCpf;
        this.targetCpf = targetCpf;
        this.amount = amount;
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
}