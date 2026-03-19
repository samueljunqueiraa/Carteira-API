package com.carteira.controller;

import com.carteira.controller.dto.request.CreateWalletRequest;
import com.carteira.controller.dto.response.PageResponse;
import com.carteira.controller.dto.response.TransactionResponse;
import com.carteira.controller.dto.response.WalletResponse;
import com.carteira.service.TransactionService;
import com.carteira.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "wallets")
@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    public WalletController(WalletService walletService, TransactionService transactionService) {
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WalletResponse create(@Valid @RequestBody CreateWalletRequest request) {
        return WalletResponse.from(walletService.createWallet(request.getName(), request.getCpf()));
    }

    @GetMapping("/{cpf}")
    public WalletResponse findByCpf(@PathVariable String cpf) {
        return WalletResponse.from(walletService.findByCpf(cpf));
    }

    @GetMapping("/{cpf}/statement")
    public PageResponse<TransactionResponse> getStatement(
            @PathVariable String cpf,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return PageResponse.from(transactionService.getStatement(cpf, pageable), TransactionResponse::from);
    }
}
