package com.carteira.domain.exception;

public class SameWalletTransferException extends RuntimeException {

    public SameWalletTransferException() {
        super("Não é permitido transferir para a própria carteira.");
    }
}