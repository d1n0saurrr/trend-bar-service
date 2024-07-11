package org.example.trendbarservice.storage;

import org.example.trendbarservice.model.Quote;
import org.springframework.stereotype.Repository;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Repository
public class QuoteStorage {
    private final BlockingQueue<Quote> queue = new LinkedBlockingQueue<>();

    public void saveQuote(Quote quote) {
        queue.add(quote);
    }

    public synchronized Quote takeQuote() throws InterruptedException {
        return queue.take();
    }
}
