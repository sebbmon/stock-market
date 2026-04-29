package com.example.stockexchange.dto;

import java.util.List;

public record WalletResponse(String id, List<StockDto> stocks) {}
