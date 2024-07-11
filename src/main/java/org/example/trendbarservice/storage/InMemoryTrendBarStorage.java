package org.example.trendbarservice.storage;

import org.example.trendbarservice.model.TrendBar;
import org.example.trendbarservice.model.TrendBarPeriod;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/***
 * Can be replaced with DB
 */
@Repository
public class InMemoryTrendBarStorage {
    ConcurrentHashMap<String, ConcurrentHashMap<TrendBarPeriod, List<TrendBar>>> storage = new ConcurrentHashMap<>();

    public void saveTrendBar(TrendBar trendBar) {
        storage.computeIfAbsent(trendBar.getSymbol(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(trendBar.getPeriod(), innerK -> new ArrayList<>())
                .add(trendBar);
    }

    public List<TrendBar> getTrendBars(String symbol, String period, Instant fromTimestamp, Instant toTimestamp) {
        return storage.getOrDefault(symbol, new ConcurrentHashMap<>())
                .getOrDefault(TrendBarPeriod.valueOf(period), Collections.emptyList())
                .stream()
                .filter(tb -> !tb.getTimestamp().isBefore(fromTimestamp) &&
                        (toTimestamp == null || !toTimestamp.isAfter(tb.getTimestamp())))
                .toList();
    }
}
