package org.example.trendbarservice.model;

import java.time.Instant;

public record Quote(String symbol, double price, Instant timestamp) {
}
