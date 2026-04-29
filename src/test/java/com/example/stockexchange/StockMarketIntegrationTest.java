package com.example.stockexchange;

import com.example.stockexchange.dto.BankState;
import com.example.stockexchange.dto.StockDto;
import com.example.stockexchange.dto.TransactionRequest;
import com.example.stockexchange.entity.BankStock;
import com.example.stockexchange.repository.AuditLogRepository;
import com.example.stockexchange.repository.BankStockRepository;
import com.example.stockexchange.repository.WalletRepository;
import com.example.stockexchange.repository.WalletStockRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StockMarketIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankStockRepository bankStockRepository;

    @Autowired
    private WalletStockRepository walletStockRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldSuccessfullyBuyAndSellStock() throws Exception {
        String testStock = "UNIQUE_MSFT_" + System.currentTimeMillis();
        String testWallet = "UNIQUE_WALLET_" + System.currentTimeMillis();

        bankStockRepository.save(new BankStock(testStock, 10));

        // Buy 1
        mockMvc.perform(post("/wallets/" + testWallet + "/stocks/" + testStock)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest("buy"))))
                .andExpect(status().isOk());

        // Verify Wallet
        mockMvc.perform(get("/wallets/" + testWallet + "/stocks/" + testStock))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        // Sell 1
        mockMvc.perform(post("/wallets/" + testWallet + "/stocks/" + testStock)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest("sell"))))
                .andExpect(status().isOk());

        // Verify Wallet is 0
        mockMvc.perform(get("/wallets/" + testWallet + "/stocks/" + testStock))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));

        // Verify Log
        mockMvc.perform(get("/log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.log[?(@.stock_name == '" + testStock + "' && @.type == 'buy')]").exists())
                .andExpect(jsonPath("$.log[?(@.stock_name == '" + testStock + "' && @.type == 'sell')]").exists());
    }

    @Test
    void shouldReturn404WhenStockNotFound() throws Exception {
        mockMvc.perform(post("/wallets/wallet123/stocks/UNKNOWN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest("buy"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenSellingWithoutStock() throws Exception {
        // Setup Bank
        bankStockRepository.save(new BankStock("AAPL", 100));

        // Sell without buying first
        mockMvc.perform(post("/wallets/wallet123/stocks/AAPL")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransactionRequest("sell"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConcurrentBuysWithPessimisticLocking() throws Exception {
        String testStock = "UNIQUE_TSLA_" + System.currentTimeMillis();
        String testWallet = "UNIQUE_CONCURRENT_WALLET_" + System.currentTimeMillis();

        // Setup Bank
        bankStockRepository.save(new BankStock(testStock, 100));

        int threadCount = 40;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    mockMvc.perform(post("/wallets/" + testWallet + "/stocks/" + testStock)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new TransactionRequest("buy"))));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // Verify that exactly 40 TSLA stocks were bought
        BankStock bankStock = bankStockRepository.findById(testStock).orElseThrow();
        assertThat(bankStock.getQuantity()).isEqualTo(60);

        Integer walletQuantity = walletStockRepository.findByWalletId(testWallet).stream()
                .filter(ws -> ws.getStockName().equals(testStock))
                .findFirst().get().getQuantity();
        assertThat(walletQuantity).isEqualTo(40);

        long logCount = auditLogRepository.findAll().stream()
                .filter(l -> l.getStockName().equals(testStock))
                .count();
        assertThat(logCount).isEqualTo(40);
    }
}
