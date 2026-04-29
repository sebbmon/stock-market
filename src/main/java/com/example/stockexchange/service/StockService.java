package com.example.stockexchange.service;

import com.example.stockexchange.entity.AuditLog;
import com.example.stockexchange.entity.BankStock;
import com.example.stockexchange.entity.Wallet;
import com.example.stockexchange.entity.WalletStock;
import com.example.stockexchange.exception.InsufficientStockException;
import com.example.stockexchange.exception.InsufficientWalletStockException;
import com.example.stockexchange.exception.StockNotFoundException;
import com.example.stockexchange.repository.AuditLogRepository;
import com.example.stockexchange.repository.BankStockRepository;
import com.example.stockexchange.repository.WalletRepository;
import com.example.stockexchange.repository.WalletStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final BankStockRepository bankStockRepository;
    private final WalletRepository walletRepository;
    private final WalletStockRepository walletStockRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void buyStock(String walletId, String stockName) {
        BankStock bankStock = bankStockRepository.findByName(stockName)
                .orElseThrow(() -> new StockNotFoundException("Stock not found in bank: " + stockName));

        if (bankStock.getQuantity() <= 0) {
            throw new InsufficientStockException("Bank has 0 stock of " + stockName);
        }

        Wallet wallet = walletRepository.findById(walletId).orElseGet(() -> {
            Wallet newWallet = new Wallet(walletId);
            return walletRepository.save(newWallet);
        });

        WalletStock walletStock = walletStockRepository.findByWalletIdAndStockName(walletId, stockName)
                .orElse(new WalletStock(wallet, stockName, 0));

        bankStock.setQuantity(bankStock.getQuantity() - 1);
        walletStock.setQuantity(walletStock.getQuantity() + 1);

        bankStockRepository.save(bankStock);
        walletStockRepository.save(walletStock);

        auditLogRepository.save(new AuditLog("buy", walletId, stockName));
    }

    @Transactional
    public void sellStock(String walletId, String stockName) {
        BankStock bankStock = bankStockRepository.findByName(stockName)
                .orElseThrow(() -> new StockNotFoundException("Stock not found in bank: " + stockName));

        WalletStock walletStock = walletStockRepository.findByWalletIdAndStockName(walletId, stockName)
                .orElseThrow(() -> new InsufficientWalletStockException("Wallet has 0 stock of " + stockName));

        if (walletStock.getQuantity() <= 0) {
            throw new InsufficientWalletStockException("Wallet has 0 stock of " + stockName);
        }

        bankStock.setQuantity(bankStock.getQuantity() + 1);
        walletStock.setQuantity(walletStock.getQuantity() - 1);

        bankStockRepository.save(bankStock);
        walletStockRepository.save(walletStock);

        auditLogRepository.save(new AuditLog("sell", walletId, stockName));
    }

    @Transactional(readOnly = true)
    public List<BankStock> getBankStocks() {
        return bankStockRepository.findAll();
    }

    @Transactional
    public void setBankStocks(List<BankStock> stocks) {
        bankStockRepository.deleteAll();
        bankStockRepository.saveAll(stocks);
    }

    @Transactional(readOnly = true)
    public Wallet getWallet(String walletId) {
        return walletRepository.findById(walletId).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<WalletStock> getWalletStocks(String walletId) {
        return walletStockRepository.findByWalletId(walletId);
    }

    @Transactional(readOnly = true)
    public Integer getWalletStockQuantity(String walletId, String stockName) {
        return walletStockRepository.findByWalletId(walletId).stream()
                .filter(ws -> ws.getStockName().equals(stockName))
                .findFirst()
                .map(WalletStock::getQuantity)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> getLogs() {
        return auditLogRepository.findAll();
    }
}
