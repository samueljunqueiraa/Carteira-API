package com.carteira.domain.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String cpf) {
        super("Carteira não encontrada para o CPF: " + cpf);
    }
}
