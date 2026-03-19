package com.carteira.service;

import com.carteira.domain.Wallet;
import com.carteira.domain.exception.CpfAlreadyExistsException;
import com.carteira.domain.exception.InvalidCpfException;
import com.carteira.domain.exception.WalletNotFoundException;
import com.carteira.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Wallet createWallet(String name, String cpf) {
        validateCpf(cpf);

        if (walletRepository.existsByCpf(cpf)) {
            throw new CpfAlreadyExistsException(cpf);
        }

        return walletRepository.save(new Wallet(name, cpf));
    }

    public Wallet findByCpf(String cpf) {
        return walletRepository.findByCpf(cpf)
                .orElseThrow(() -> new WalletNotFoundException(cpf));
    }

    private void validateCpf(String cpf) {
        String digits = cpf.replaceAll("[^0-9]", "");

        if (digits.length() != 11) {
            throw new InvalidCpfException(cpf);
        }

        // Rejeita sequências com todos os dígitos iguais (ex: 111.111.111-11)
        if (digits.chars().distinct().count() == 1) {
            throw new InvalidCpfException(cpf);
        }

        // Primeiro dígito verificador
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += (digits.charAt(i) - '0') * (10 - i);
        }
        int remainder = sum % 11;
        int firstDigit = remainder < 2 ? 0 : 11 - remainder;

        if (firstDigit != (digits.charAt(9) - '0')) {
            throw new InvalidCpfException(cpf);
        }

        // Segundo dígito verificador
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += (digits.charAt(i) - '0') * (11 - i);
        }
        remainder = sum % 11;
        int secondDigit = remainder < 2 ? 0 : 11 - remainder;

        if (secondDigit != (digits.charAt(10) - '0')) {
            throw new InvalidCpfException(cpf);
        }
    }
}