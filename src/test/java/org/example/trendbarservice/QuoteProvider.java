package org.example.trendbarservice;

import org.example.trendbarservice.model.Quote;
import org.example.trendbarservice.storage.QuoteStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class QuoteProvider {
    private final QuoteStorage quoteStorage;
    private final ScheduledExecutorService scheduler;
    private final Random random;

    public static final String[] SYMBOLS  = {"EURUSD", "EURGBP", "EURJPY", "EURRUB", "EURAED", "USDGBP", "USDJPY",
            "USDRUB", "USDAED", "GBPJPY", "GBPRUB", "GBPAED", "JPYRUB", "JPYAED", "RUBAED"};

    @Autowired
    public QuoteProvider (QuoteStorage quoteStorage) {
        this.quoteStorage = quoteStorage;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.random = new Random();
    }

    private double getRandomPrice() {
        return 100.0 + random.nextDouble() * 10;
    }

    public void generateQuotesForEverySymbol() {
        List<String> symbols = Arrays.asList(SYMBOLS);
        Collections.shuffle(symbols);
        for (String symbol : symbols) {
            quoteStorage.saveQuote(new Quote(symbol, getRandomPrice(), Instant.now()));
        }
    }

    public void generateQuotes(String symbol, int count, long delay) {
        for (int i = 0; i < count; i++) {
            quoteStorage.saveQuote(new Quote(symbol, getRandomPrice(), Instant.now()));
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Map<String, List<Quote>> generateQuotesInPeriod(int count, Instant from, Instant to, long delay) {
        Map<String, List<Quote>> quotes = new HashMap<>(count * SYMBOLS.length);
        long differ = ChronoUnit.MILLIS.between(from, to) / count;
        for (int i = 0; i < count; i++) {
            for (String symbol : SYMBOLS) {
                Quote quote = new Quote(symbol, getRandomPrice(), from.plusMillis(differ * i));
                quotes.computeIfAbsent(symbol, k -> new ArrayList<>()).add(quote);
                quoteStorage.saveQuote(quote);
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return quotes;
    }

    public void start() {
        Instant now = Instant.now();
        Instant nextSec = Instant.now().plus(1, ChronoUnit.SECONDS).truncatedTo(ChronoUnit.SECONDS);
        long initialDelay = ChronoUnit.MILLIS.between(now, nextSec);
        scheduler.scheduleAtFixedRate(this::generateQuotesForEverySymbol, initialDelay, 1000, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }
}
