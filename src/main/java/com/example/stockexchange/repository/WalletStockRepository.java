package com.example.stockexchange.repository;

import com.example.stockexchange.entity.WalletStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface WalletStockRepository extends JpaRepository<WalletStock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletStock> findByWalletIdAndStockName(String walletId, String stockName);
    
    List<WalletStock> findByWalletId(String walletId);
}
