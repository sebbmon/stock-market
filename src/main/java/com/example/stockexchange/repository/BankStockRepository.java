package com.example.stockexchange.repository;

import com.example.stockexchange.entity.BankStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface BankStockRepository extends JpaRepository<BankStock, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankStock> findByName(String name);
}
