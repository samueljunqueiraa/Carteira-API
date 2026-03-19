package com.carteira.domain.exception;

public class CpfAlreadyExistsException extends RuntimeException {

    public CpfAlreadyExistsException(String cpf) {
        super("Já existe uma carteira cadastrada com o CPF: " + cpf);
    }
}