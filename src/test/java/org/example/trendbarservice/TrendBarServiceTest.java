package org.example.trendbarservice;

import org.example.trendbarservice.model.TrendBar;
import org.example.trendbarservice.service.TrendBarService;
import org.example.trendbarservice.storage.InMemoryTrendBarStorage;
import org.example.trendbarservice.storage.QuoteStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class TrendBarServiceTest {
    private final QuoteProvider quoteProvider;
    private final QuoteStorage quoteStorage;
    private final TrendBarService trendBarService;
    private final InMemoryTrendBarStorage inMemoryTrendBarStorage;

    @Autowired
    public TrendBarServiceTest(QuoteProvider quoteProvider, QuoteStorage quoteStorage, TrendBarService trendBarService,
                               InMemoryTrendBarStorage inMemoryTrendBarStorage) {
        this.quoteProvider = quoteProvider;
        this.trendBarService = trendBarService;
        this.quoteStorage = quoteStorage;
        this.inMemoryTrendBarStorage = inMemoryTrendBarStorage;
    }

    @BeforeEach
    public void setUp() {
        quoteProvider.start();
    }

    @AfterEach
    public void tearDown() {
        quoteProvider.stop();
    }

    @Test
    public void trendBarStorageNotEmpty() throws InterruptedException {
        // Give quoteProvider some time to generate quotes
        Thread.sleep(1000);

        // Quote storage should not be empty
        assert !quoteStorage.isEmpty();
    }

    @Test
    public void testQuoteProcessing() throws InterruptedException {
        // Give quoteProvider some time to generate quotes
        Thread.sleep(1000);

        // TBs history should not be empty
        List<TrendBar> trendBars = inMemoryTrendBarStorage.getTrendBars("EURUSD", "M1",
                System.currentTimeMillis() - 60000, null);

        assert !trendBars.isEmpty();
    }
}
