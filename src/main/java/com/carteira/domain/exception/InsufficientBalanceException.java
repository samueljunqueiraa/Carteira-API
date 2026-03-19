package com.carteira.domain.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(BigDecimal balance, BigDecimal amount) {
        super("Saldo insuficiente. Saldo disponível: " + balance + ", valor solicitado: " + amount);
    }
}