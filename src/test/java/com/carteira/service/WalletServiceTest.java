package com.carteira.service;

import com.carteira.domain.Wallet;
import com.carteira.domain.exception.CpfAlreadyExistsException;
import com.carteira.domain.exception.InvalidCpfException;
import com.carteira.domain.exception.WalletNotFoundException;
import com.carteira.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    // CPFs válidos para uso nos testes
    private static final String VALID_CPF = "529.982.247-25";
    private static final String ANOTHER_VALID_CPF = "111.444.777-35";

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        wallet = new Wallet("João Silva", VALID_CPF);
    }

    @Test
    void createWallet_whenValidData_shouldReturnSavedWallet() {
        when(walletRepository.existsByCpf(VALID_CPF)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        Wallet result = walletService.createWallet("João Silva", VALID_CPF);

        assertThat(result.getName()).isEqualTo("João Silva");
        assertThat(result.getCpf()).isEqualTo(VALID_CPF);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_whenCpfHasInvalidCheckDigits_shouldThrowInvalidCpfException() {
        assertThatThrownBy(() -> walletService.createWallet("João Silva", "529.982.247-00"))
                .isInstanceOf(InvalidCpfException.class)
                .hasMessageContaining("CPF inválido");

        verify(walletRepository, never()).save(any());
    }

    @Test
    void createWallet_whenCpfHasAllSameDigits_shouldThrowInvalidCpfException() {
        assertThatThrownBy(() -> walletService.createWallet("João Silva", "111.111.111-11"))
                .isInstanceOf(InvalidCpfException.class);

        verify(walletRepository, never()).save(any());
    }

    @Test
    void createWallet_whenCpfHasWrongFormat_shouldThrowInvalidCpfException() {
        assertThatThrownBy(() -> walletService.createWallet("João Silva", "12345678900"))
                .isInstanceOf(InvalidCpfException.class);

        verify(walletRepository, never()).save(any());
    }

    @Test
    void createWallet_whenCpfAlreadyExists_shouldThrowCpfAlreadyExistsException() {
        when(walletRepository.existsByCpf(VALID_CPF)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWallet("João Silva", VALID_CPF))
                .isInstanceOf(CpfAlreadyExistsException.class)
                .hasMessageContaining(VALID_CPF);

        verify(walletRepository, never()).save(any());
    }

    @Test
    void findByCpf_whenWalletExists_shouldReturnWallet() {
        when(walletRepository.findByCpf(VALID_CPF)).thenReturn(Optional.of(wallet));

        Wallet result = walletService.findByCpf(VALID_CPF);

        assertThat(result.getCpf()).isEqualTo(VALID_CPF);
    }

    @Test
    void findByCpf_whenWalletNotFound_shouldThrowWalletNotFoundException() {
        when(walletRepository.findByCpf(VALID_CPF)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.findByCpf(VALID_CPF))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining(VALID_CPF);
    }
}
