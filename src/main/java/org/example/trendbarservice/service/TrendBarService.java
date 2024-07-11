package org.example.trendbarservice.service;

import org.example.trendbarservice.model.Quote;
import org.example.trendbarservice.model.SymbolTime;
import org.example.trendbarservice.model.TrendBar;
import org.example.trendbarservice.storage.QuoteStorage;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TrendBarService implements InitializingBean, DisposableBean {
    private final QuoteStorage quoteStorage;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<SymbolTime, TrendBar> currentTBs;

    private final int CONCURRENCY = 10;

    @Autowired
    public TrendBarService(QuoteStorage quoteStorage) {
        this.quoteStorage = quoteStorage;
        this.executorService = Executors.newFixedThreadPool(CONCURRENCY);
        this.currentTBs = new ConcurrentHashMap<>();
    }

    public final ConcurrentHashMap<SymbolTime, TrendBar> getCurrentTBs() {
        return currentTBs;
    }

    @Override
    public void afterPropertiesSet() {
        for (int i = 0; i < CONCURRENCY; i++) {
            executorService.submit(() -> {
                while (true) {
                    try {
                        Quote quote = quoteStorage.takeQuote();
                        processQuote(quote);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    private void processQuote(Quote quote) {
        Instant truncatedToMinutes = quote.getTimestamp().truncatedTo(ChronoUnit.MINUTES);
        currentTBs.compute(new SymbolTime(quote.getSymbol(), truncatedToMinutes), (k, tb) -> {
            if (tb != null) {
                return tb.updatePrices(quote);
            } else {
                return new TrendBar(quote);
            }
        });
    }

    @Override
    public void destroy() {
        executorService.shutdownNow();
    }
}
