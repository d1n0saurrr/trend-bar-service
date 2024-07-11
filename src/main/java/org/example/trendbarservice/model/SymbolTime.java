package org.example.trendbarservice.model;

import java.time.Instant;

public record SymbolTime(String symbol, Instant timestamp) {
}
