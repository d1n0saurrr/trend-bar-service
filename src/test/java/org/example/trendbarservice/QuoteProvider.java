package org.example.trendbarservice;

import org.example.trendbarservice.model.Quote;
import org.example.trendbarservice.storage.QuoteStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class QuoteProvider {
    private final QuoteStorage quoteStorage;
    private final ScheduledExecutorService scheduler;
    private final Random random;

    private final String[] SYMBOLS  = {"EURUSD", "GBPUSD", "USDJPY", "GBPJPY", "EURGBP", "JPYEUR"};

    @Autowired
    public QuoteProvider (QuoteStorage quoteStorage) {
        this.quoteStorage = quoteStorage;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.random = new Random();
    }

    private void generateQuote() {
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
        double price = 1.0 + (random.nextDouble() * 0.1);
        long timestamp = System.currentTimeMillis();

        Quote quote = new Quote(symbol, price, timestamp);
        quoteStorage.saveQuote(quote);
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::generateQuote, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
