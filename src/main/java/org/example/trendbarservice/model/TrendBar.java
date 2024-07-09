package org.example.trendbarservice.model;

public class TrendBar {
    private String symbol;
    private double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;
    private TrendBarPeriod period;
    private long timestamp;

    public TrendBar(String symbol, double openPrice, double closePrice, double highPrice,
                    double lowPrice, TrendBarPeriod period, long timestamp) {
        this.symbol = symbol;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.period = period;
        this.timestamp = timestamp;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public TrendBarPeriod getPeriod() {
        return period;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
