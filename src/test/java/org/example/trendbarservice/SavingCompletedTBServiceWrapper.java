package org.example.trendbarservice;

import org.example.trendbarservice.service.SavingCompletedTBService;
import org.example.trendbarservice.service.TrendBarService;
import org.example.trendbarservice.storage.InMemoryTrendBarStorage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SavingCompletedTBServiceWrapper extends SavingCompletedTBService {
    public SavingCompletedTBServiceWrapper(InMemoryTrendBarStorage inMemoryTrendBarStorage, TrendBarService trendBarService) {
        super(inMemoryTrendBarStorage, trendBarService);
    }

    @Override
    public void afterPropertiesSet() {}

    public void saveCompletedTBs(Instant instant) {
        Instant currentMinute = instant.truncatedTo(ChronoUnit.MINUTES);
        trendBarService.getCurrentTBs().entrySet().removeIf(entry -> {
            if (currentMinute.isAfter(entry.getKey().timestamp())) {
                processFinishingM1Period(entry, currentMinute);
                return true;
            } else {
                return false;
            }
        });
    }
}
