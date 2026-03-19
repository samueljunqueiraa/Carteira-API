package com.carteira.domain.exception;

public class InvalidCpfException extends RuntimeException {

    public InvalidCpfException(String cpf) {
        super("CPF inválido: " + cpf);
    }
}
