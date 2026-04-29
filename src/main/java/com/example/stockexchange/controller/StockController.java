package com.example.stockexchange.controller;

import com.example.stockexchange.dto.BankState;
import com.example.stockexchange.dto.LogDto;
import com.example.stockexchange.dto.LogResponse;
import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.entity.BankStock;
import com.example.stockexchange.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/stocks")
    public BankState getStocks() {
        List<StockDto> stockDtos = stockService.getBankStocks().stream()
                .map(s -> new StockDto(s.getName(), s.getQuantity()))
                .toList();
        return new BankState(stockDtos);
    }

    @PostMapping("/stocks")
    public void setStocks(@RequestBody BankState bankState) {
        List<BankStock> stocks = bankState.stocks().stream()
                .map(s -> new BankStock(s.name(), s.quantity()))
                .toList();
        stockService.setBankStocks(stocks);
    }

    @GetMapping("/log")
    public LogResponse getLog() {
        List<LogDto> logs = stockService.getLogs().stream()
                .map(l -> new LogDto(l.getType(), l.getWalletId(), l.getStockName()))
                .toList();
        return new LogResponse(logs);
    }

    @PostMapping("/chaos")
    public void chaos() {
        System.exit(1);
    }
}
