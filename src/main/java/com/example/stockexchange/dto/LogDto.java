package com.example.stockexchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogDto(
        String type, 
        @JsonProperty("wallet_id") String walletId, 
        @JsonProperty("stock_name") String stockName
) {}
