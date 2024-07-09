package org.example.trendbarservice.storage;

import org.example.trendbarservice.model.TrendBar;
import org.springframework.stereotype.Repository;

import java.util.*;

/***
 * Can be replaced with DB
 */
@Repository
public class InMemoryTrendBarStorage {
    public synchronized void saveTrendBar(TrendBar trendBar) {
    }

    public synchronized List<TrendBar> getTrendBars(String symbol, String period, Long fromTimestamp, Long toTimestamp) {
        return List.of();
    }
}
