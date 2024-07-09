package org.example.trendbarservice.storage;

import org.example.trendbarservice.model.Quote;
import org.springframework.stereotype.Repository;

@Repository
public class QuoteStorage {
    public void saveQuote(Quote quote) {
    }

    public Quote takeQuote() {
        return null;
    }

    public boolean isEmpty() {
        return true;
    }
}
