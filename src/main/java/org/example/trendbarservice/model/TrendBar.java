package org.example.trendbarservice.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TrendBar {
    private final String symbol;
    private final double openPrice;
    private double closePrice;
    private double highPrice;
    private double lowPrice;
    private TrendBarPeriod period;
    private Instant timestamp;

    public TrendBar(String symbol, double openPrice, double closePrice, double highPrice,
                    double lowPrice, TrendBarPeriod period, Instant timestamp) {
        this.symbol = symbol;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.period = period;
        this.timestamp = timestamp;
    }

    public TrendBar(Quote quote) {
        this.symbol = quote.getSymbol();
        this.openPrice = quote.getPrice();
        this.closePrice = quote.getPrice();
        this.highPrice = quote.getPrice();
        this.lowPrice = quote.getPrice();
        this.period = TrendBarPeriod.M1;
        this.timestamp = quote.getTimestamp().truncatedTo(ChronoUnit.MINUTES);
    }

    public static TrendBar of(TrendBar trendBar, TrendBarPeriod period) {
        return new TrendBar(trendBar.symbol, trendBar.openPrice, trendBar.closePrice, trendBar.highPrice,
                trendBar.lowPrice, period, period.truncateTimestamp(trendBar.timestamp));
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public TrendBar updatePrices(Quote quote) {
        if (quote.getPrice() > highPrice) {
            highPrice = quote.getPrice();
        }

        if (quote.getPrice() < lowPrice) {
            lowPrice = quote.getPrice();
        }

        closePrice = quote.getPrice();

        return this;
    }

    public TrendBar updatePrices(TrendBar trendBar, TrendBarPeriod newPeriod) {
        if (trendBar.getHighPrice() > highPrice) {
            highPrice = trendBar.getHighPrice();
        }

        if (trendBar.getLowPrice() < lowPrice) {
            lowPrice = trendBar.getLowPrice();
        }

        closePrice = trendBar.getClosePrice();
        period = newPeriod;
        timestamp = newPeriod.truncateTimestamp(timestamp);

        return this;
    }
}
