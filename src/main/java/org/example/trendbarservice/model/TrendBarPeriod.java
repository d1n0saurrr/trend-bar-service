package org.example.trendbarservice.model;

public enum TrendBarPeriod {
    M1 (60_000),
    H1 (3_600_000),
    D1 (86_400_000);

    private final long millis;

    TrendBarPeriod(long millis) {
        this.millis = millis;
    }

    private long getMillis() {
        return millis;
    }
}
