package com.carteira.service;

import com.carteira.domain.Transaction;
import com.carteira.domain.Wallet;
import com.carteira.domain.exception.InsufficientBalanceException;
import com.carteira.domain.exception.SameWalletTransferException;
import com.carteira.domain.exception.WalletNotFoundException;
import com.carteira.repository.TransactionRepository;
import com.carteira.repository.WalletRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(WalletRepository walletRepository,
                              TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Transaction transfer(String sourceCpf, String targetCpf, BigDecimal amount) {
        if (sourceCpf.equals(targetCpf)) {
            throw new SameWalletTransferException();
        }

        Wallet source = walletRepository.findByCpf(sourceCpf)
                .orElseThrow(() -> new WalletNotFoundException(sourceCpf));

        Wallet target = walletRepository.findByCpf(targetCpf)
                .orElseThrow(() -> new WalletNotFoundException(targetCpf));

        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(source.getBalance(), amount);
        }

        source.debit(amount);
        target.credit(amount);

        walletRepository.save(source);
        walletRepository.save(target);

        return transactionRepository.save(new Transaction(source, target, amount));
    }

    @Transactional(readOnly = true)
    public Page<Transaction> getStatement(String cpf, Pageable pageable) {
        Wallet wallet = walletRepository.findByCpf(cpf)
                .orElseThrow(() -> new WalletNotFoundException(cpf));

        return transactionRepository.findByWallet(wallet, pageable);
    }
}