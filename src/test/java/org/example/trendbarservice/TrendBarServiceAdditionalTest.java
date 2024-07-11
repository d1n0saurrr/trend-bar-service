package org.example.trendbarservice;

import org.example.trendbarservice.model.Quote;
import org.example.trendbarservice.model.TrendBar;
import org.example.trendbarservice.storage.InMemoryTrendBarStorage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.*;
import static org.example.trendbarservice.model.TrendBarPeriod.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TrendBarServiceAdditionalTest {
    private final QuoteProvider quoteProvider;
    private final SavingCompletedTBServiceWrapper savingCompletedTBServiceWrapper;
    private final InMemoryTrendBarStorage inMemoryTrendBarStorage;

    @Autowired
    public TrendBarServiceAdditionalTest(QuoteProvider quoteProvider,
                                         SavingCompletedTBServiceWrapper savingCompletedTBServiceWrapper,
                                         InMemoryTrendBarStorage inMemoryTrendBarStorage) {
        this.quoteProvider = quoteProvider;
        this.savingCompletedTBServiceWrapper = savingCompletedTBServiceWrapper;
        this.inMemoryTrendBarStorage = inMemoryTrendBarStorage;
    }

    @Test
    public void testTBHistoryNotEmpty_when1SymbolOnlyEverySec() {
        // Generate quotes for given symbol every delay millis
        String symbol = "USDJPY";
        quoteProvider.generateQuotes(symbol, 10, 100);

        // Wil not wait till TBs are saved to InMemoryStorage - call saving manually
        savingCompletedTBServiceWrapper.saveCompletedTBs(Instant.now().plusSeconds(60));

        // Assert existing history for given symbol
        Assertions.assertFalse(
                inMemoryTrendBarStorage.getTrendBars(symbol, M1.name(),
                        Instant.now().minus(2, MINUTES), null).isEmpty(),
                "InMemoryTrendBarsStorage is empty for generated quotes");
    }

    @Test
    public void testTBHistoryCorrectM1_whenAllSymbols() {
        // Generate quotes for given symbol every delay millis
        Instant from = Instant.now().minusSeconds(120).truncatedTo(MINUTES);
        Instant to = Instant.now().minusSeconds(60).truncatedTo(MINUTES);
        Map<String, List<Quote>> quotes = quoteProvider.generateQuotesInPeriod(10, from, to, 100);

        // Wil not wait till TBs are saved to InMemoryStorage - call saving manually
        savingCompletedTBServiceWrapper.saveCompletedTBs(to.plusSeconds(10));

        // Assert TBs are correct
        for (var entry : quotes.entrySet()) {
            double openPrice = entry.getValue().get(0).getPrice();
            double closePrice = entry.getValue().get(entry.getValue().size() - 1).getPrice();
            double highPrice = entry.getValue().stream().mapToDouble(Quote::getPrice).max().orElse(Double.NaN);
            double lowPrice = entry.getValue().stream().mapToDouble(Quote::getPrice).min().orElse(Double.NaN);
            Instant timestamp = entry.getValue().get(0).getTimestamp().truncatedTo(MINUTES);
            TrendBar tb = new TrendBar(entry.getKey(), openPrice, closePrice, highPrice, lowPrice, M1, timestamp);

            List<TrendBar> trendBars = inMemoryTrendBarStorage.getTrendBars(entry.getKey(), "M1", from, null);
            assertEquals(1, trendBars.size(), "TB history upon every symbol should be of size 1");
            assertEqualsTrendBars(tb, trendBars.get(0));
        }
    }

    private void assertEqualsTrendBars(TrendBar tb1, TrendBar tb2) {
        assertEquals(tb1.getSymbol(), tb2.getSymbol(), "TB is incorrect: symbol");
        assertEquals(tb1.getTimestamp(), tb2.getTimestamp(), "TB is incorrect: timestamp");
        assertEquals(tb1.getOpenPrice(), tb2.getOpenPrice(), "TB is incorrect: openPrice");
        assertEquals(tb1.getClosePrice(), tb2.getClosePrice(), "TB is incorrect: closePrice");
        assertEquals(tb1.getHighPrice(), tb2.getHighPrice(), "TB is incorrect: highPrice");
        assertEquals(tb1.getLowPrice(), tb2.getLowPrice(), "TB is incorrect: lowPrice");
    }

    @Test
    public void testTBHistoryCorrectAllPeriods_whenAllSymbols() {
        // Generate 100 quotes for all symbols -2 days from 23:50 to 00:00
        Instant from = Instant.now().minus(2, DAYS).truncatedTo(DAYS)
                .plus(23, HOURS).plus(50, MINUTES);
        Instant to = Instant.now().minus(2, DAYS).truncatedTo(DAYS).plus(24, HOURS);
        Map<String, List<Quote>> quotes = quoteProvider.generateQuotesInPeriod(100, from, to, 100);

        // Wil not wait till TBs are saved to InMemoryStorage - call saving manually for
        // every minutes so TBs complete in natural order
        for (int i = 0; i < 11; i++) {
            savingCompletedTBServiceWrapper.saveCompletedTBs(from.plusSeconds(60 * i));
        }

        // Assert TBs are correct
        for (var entry : quotes.entrySet()) {
            // Count TBs manually for comparing
            List<TrendBar> minuteTBs = entry.getValue().stream()
                    .collect(Collectors.groupingBy(q -> q.getTimestamp().truncatedTo(MINUTES)))
                    .entrySet().stream()
                    .map(e -> {
                        List<Quote> periodQuotes = e.getValue();
                        Instant timestamp = e.getKey();
                        double openPrice = periodQuotes.get(0).getPrice();
                        double closePrice = periodQuotes.get(periodQuotes.size() - 1).getPrice();
                        double highPrice = periodQuotes.stream().mapToDouble(Quote::getPrice).max().orElse(Double.NaN);
                        double lowPrice = periodQuotes.stream().mapToDouble(Quote::getPrice).min().orElse(Double.NaN);
                        return new TrendBar(entry.getKey(), openPrice, closePrice, highPrice, lowPrice, M1, timestamp);
                    })
                    .sorted(Comparator.comparing(TrendBar::getTimestamp))
                    .toList();

            List<TrendBar> hoursTBs = minuteTBs.stream()
                    .collect(Collectors.groupingBy(tb -> tb.getTimestamp().truncatedTo(HOURS)))
                    .entrySet().stream()
                    .map(e -> {
                        List<TrendBar> trendBars = e.getValue();
                        Instant timestamp = e.getKey();
                        double openPrice = trendBars.get(0).getOpenPrice();
                        double closePrice = trendBars.get(trendBars.size() - 1).getClosePrice();
                        double highPrice = trendBars.stream().mapToDouble(TrendBar::getHighPrice).max().orElse(Double.NaN);
                        double lowPrice = trendBars.stream().mapToDouble(TrendBar::getLowPrice).min().orElse(Double.NaN);
                        return new TrendBar(entry.getKey(), openPrice, closePrice, highPrice, lowPrice, H1, timestamp);
                    })
                    .sorted(Comparator.comparing(TrendBar::getTimestamp))
                    .toList();

            List<TrendBar> daysTBs = hoursTBs.stream()
                    .collect(Collectors.groupingBy(tb -> tb.getTimestamp().truncatedTo(DAYS)))
                    .entrySet().stream()
                    .map(e -> {
                        List<TrendBar> trendBars = e.getValue();
                        Instant timestamp = e.getKey();
                        double openPrice = trendBars.get(0).getOpenPrice();
                        double closePrice = trendBars.get(trendBars.size() - 1).getClosePrice();
                        double highPrice = trendBars.stream().mapToDouble(TrendBar::getHighPrice).max().orElse(Double.NaN);
                        double lowPrice = trendBars.stream().mapToDouble(TrendBar::getLowPrice).min().orElse(Double.NaN);
                        return new TrendBar(entry.getKey(), openPrice, closePrice, highPrice, lowPrice, D1, timestamp);
                    })
                    .sorted(Comparator.comparing(TrendBar::getTimestamp))
                    .toList();

            // Assert lists has same element
            List<TrendBar> trendBars = inMemoryTrendBarStorage
                    .getTrendBars(entry.getKey(), "M1", from.truncatedTo(MINUTES), null)
                    .stream()
                    .sorted(Comparator.comparing(TrendBar::getTimestamp))
                    .toList();

            assertEquals(minuteTBs.size(), trendBars.size(), "Lists have different sizes");
            for (int i = 0; i < minuteTBs.size(); i++) {
                assertEqualsTrendBars(minuteTBs.get(i), trendBars.get(i));
            }

            trendBars = inMemoryTrendBarStorage
                    .getTrendBars(entry.getKey(), "H1", from.truncatedTo(HOURS), null)
                    .stream()
                    .sorted(Comparator.comparing(TrendBar::getTimestamp))
                    .toList();

            assertEquals(hoursTBs.size(), trendBars.size(), "Lists have different sizes");
            for (int i = 0; i < hoursTBs.size(); i++) {
                assertEqualsTrendBars(hoursTBs.get(i), trendBars.get(i));
            }

            trendBars = inMemoryTrendBarStorage
                    .getTrendBars(entry.getKey(), "D1", from.truncatedTo(DAYS), null)
                    .stream()
                    .sorted(Comparator.comparing(TrendBar::getTimestamp))
                    .toList();

            assertEquals(daysTBs.size(), trendBars.size(), "Lists have different sizes");
            for (int i = 0; i < daysTBs.size(); i++) {
                assertEqualsTrendBars(daysTBs.get(i), trendBars.get(i));
            }
        }
    }
}
