package com.carteira.service;

import com.carteira.domain.Transaction;
import com.carteira.domain.Wallet;
import com.carteira.domain.exception.InsufficientBalanceException;
import com.carteira.domain.exception.SameWalletTransferException;
import com.carteira.domain.exception.WalletNotFoundException;
import com.carteira.repository.TransactionRepository;
import com.carteira.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    private static final String SOURCE_CPF = "529.982.247-25";
    private static final String TARGET_CPF = "111.444.777-35";
    private static final BigDecimal AMOUNT = new BigDecimal("50.00");

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Wallet sourceWallet;
    private Wallet targetWallet;

    @BeforeEach
    void setUp() {
        sourceWallet = new Wallet("João Silva", SOURCE_CPF);
        sourceWallet.credit(new BigDecimal("100.00")); // saldo inicial: 100.00

        targetWallet = new Wallet("Maria Souza", TARGET_CPF);
    }

    @Test
    void transfer_whenValidData_shouldDebitSourceAndCreditTarget() {
        when(walletRepository.findByCpf(SOURCE_CPF)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByCpf(TARGET_CPF)).thenReturn(Optional.of(targetWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction result = transactionService.transfer(SOURCE_CPF, TARGET_CPF, AMOUNT);

        assertThat(sourceWallet.getBalance()).isEqualByComparingTo("50.00");
        assertThat(targetWallet.getBalance()).isEqualByComparingTo("50.00");
        assertThat(result.getAmount()).isEqualByComparingTo(AMOUNT);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_whenSameCpf_shouldThrowSameWalletTransferException() {
        assertThatThrownBy(() -> transactionService.transfer(SOURCE_CPF, SOURCE_CPF, AMOUNT))
                .isInstanceOf(SameWalletTransferException.class);

        verify(walletRepository, never()).findByCpf(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_whenSourceWalletNotFound_shouldThrowWalletNotFoundException() {
        when(walletRepository.findByCpf(SOURCE_CPF)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(SOURCE_CPF, TARGET_CPF, AMOUNT))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining(SOURCE_CPF);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_whenTargetWalletNotFound_shouldThrowWalletNotFoundException() {
        when(walletRepository.findByCpf(SOURCE_CPF)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByCpf(TARGET_CPF)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transfer(SOURCE_CPF, TARGET_CPF, AMOUNT))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining(TARGET_CPF);

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_whenInsufficientBalance_shouldThrowInsufficientBalanceException() {
        BigDecimal amountExceedingBalance = new BigDecimal("200.00");
        when(walletRepository.findByCpf(SOURCE_CPF)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByCpf(TARGET_CPF)).thenReturn(Optional.of(targetWallet));

        assertThatThrownBy(() -> transactionService.transfer(SOURCE_CPF, TARGET_CPF, amountExceedingBalance))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("100.00")
                .hasMessageContaining("200.00");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_whenBalanceIsExactlyEqualToAmount_shouldSucceed() {
        BigDecimal exactAmount = new BigDecimal("100.00");
        when(walletRepository.findByCpf(SOURCE_CPF)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByCpf(TARGET_CPF)).thenReturn(Optional.of(targetWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        transactionService.transfer(SOURCE_CPF, TARGET_CPF, exactAmount);

        assertThat(sourceWallet.getBalance()).isEqualByComparingTo("0.00");
        assertThat(targetWallet.getBalance()).isEqualByComparingTo("100.00");
    }
}
