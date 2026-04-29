package com.example.stockexchange.controller;

import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.dto.TransactionRequest;
import com.example.stockexchange.dto.WalletResponse;
import com.example.stockexchange.entity.Wallet;
import com.example.stockexchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final StockService stockService;

    @PostMapping("/{wallet_id}/stocks/{stock_name}")
    public void transact(@PathVariable("wallet_id") String walletId,
                         @PathVariable("stock_name") String stockName,
                         @RequestBody TransactionRequest request) {
        if ("buy".equalsIgnoreCase(request.type())) {
            stockService.buyStock(walletId, stockName);
        } else if ("sell".equalsIgnoreCase(request.type())) {
            stockService.sellStock(walletId, stockName);
        } else {
            throw new IllegalArgumentException("Invalid transaction type: " + request.type());
        }
    }

    @GetMapping("/{wallet_id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable("wallet_id") String walletId) {
        Wallet wallet = stockService.getWallet(walletId);
        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }
        List<StockDto> stocks = stockService.getWalletStocks(walletId).stream()
                .map(s -> new StockDto(s.getStockName(), s.getQuantity()))
                .toList();
        return ResponseEntity.ok(new WalletResponse(walletId, stocks));
    }

    @GetMapping("/{wallet_id}/stocks/{stock_name}")
    public ResponseEntity<Integer> getWalletStock(@PathVariable("wallet_id") String walletId,
                                                  @PathVariable("stock_name") String stockName) {
        Integer quantity = stockService.getWalletStockQuantity(walletId, stockName);
        if (quantity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quantity);
    }
}
