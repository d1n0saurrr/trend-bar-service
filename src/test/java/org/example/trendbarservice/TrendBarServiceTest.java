package org.example.trendbarservice;

import org.example.trendbarservice.service.TrendBarService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class TrendBarServiceTest {
    private final QuoteProvider quoteProvider;
    private final TrendBarService trendBarService;

    @Autowired
    public TrendBarServiceTest(QuoteProvider quoteProvider, TrendBarService trendBarService) {
        this.quoteProvider = quoteProvider;
        this.trendBarService = trendBarService;
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
    public void testQuoteProcessing() throws InterruptedException {
        // Give quoteProvider some time to generate quotes
        Thread.sleep(1000);

        // TBs history should not be empty
        Assertions.assertFalse(trendBarService.getCurrentTBs().isEmpty(),
                "Quotes are not processing - map of current TBs are empty");
    }
}
