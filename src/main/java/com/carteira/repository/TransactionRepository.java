package com.carteira.repository;

import com.carteira.domain.Transaction;
import com.carteira.domain.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("SELECT t FROM Transaction t WHERE t.sourceWallet = :wallet OR t.targetWallet = :wallet ORDER BY t.createdAt DESC")
    Page<Transaction> findByWallet(@Param("wallet") Wallet wallet, Pageable pageable);
}
