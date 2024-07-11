package org.example.trendbarservice.model;

import java.time.Instant;

public class Quote {
    private final String symbol;
    private final double price;
    private final Instant timestamp;

    public Quote(String symbol, double price, Instant timestamp) {
        this.symbol = symbol;
        this.price = price;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
