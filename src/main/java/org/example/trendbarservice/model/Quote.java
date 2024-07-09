package org.example.trendbarservice.model;

public class Quote {
    private String symbol;
    private double price;
    private long timestamp;

    public Quote(String symbol, double price, long timestamp) {
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

    public long getTimestamp() {
        return timestamp;
    }
}
