package org.example.trendbarservice.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum TrendBarPeriod {
    M1,
    H1,
    D1;

    public Instant truncateTimestamp(Instant timestamp) {
        return switch (this) {
            case M1 -> timestamp.truncatedTo(ChronoUnit.MINUTES);
            case H1 -> timestamp.truncatedTo(ChronoUnit.HOURS);
            case D1 -> timestamp.truncatedTo(ChronoUnit.DAYS);
        };
    }
}
