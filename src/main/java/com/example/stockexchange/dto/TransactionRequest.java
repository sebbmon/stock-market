package com.example.stockexchange.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TransactionRequest(String type) {}
