package org.example.trendbarservice.service;

import org.example.trendbarservice.model.SymbolTime;
import org.example.trendbarservice.model.TrendBar;
import org.example.trendbarservice.storage.InMemoryTrendBarStorage;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.*;
import static org.example.trendbarservice.model.TrendBarPeriod.D1;
import static org.example.trendbarservice.model.TrendBarPeriod.H1;

@Service
public class SavingCompletedTBService implements InitializingBean, DisposableBean {
    private final InMemoryTrendBarStorage inMemoryTrendBarStorage;
    protected final TrendBarService trendBarService;
    private final ScheduledExecutorService scheduler;
    private final ConcurrentHashMap<SymbolTime, TrendBar> hourTBs = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<SymbolTime, TrendBar> dayTBs = new ConcurrentHashMap<>();

    public SavingCompletedTBService(InMemoryTrendBarStorage inMemoryTrendBarStorage, TrendBarService trendBarService) {
        this.inMemoryTrendBarStorage = inMemoryTrendBarStorage;
        this.trendBarService = trendBarService;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void afterPropertiesSet() {
        Instant now = Instant.now();
        Instant nexMinute = Instant.now().plus(1, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
        long initialDelay = ChronoUnit.MILLIS.between(now, nexMinute);
        scheduler.scheduleAtFixedRate(this::saveCompletedTBs, initialDelay, 60_000, TimeUnit.MILLISECONDS);
    }

    public void saveCompletedTBs() {
        Instant currentMinute = Instant.now().truncatedTo(ChronoUnit.MINUTES);
        trendBarService.getCurrentTBs().entrySet().removeIf(entry -> {
            if (currentMinute.isAfter(entry.getKey().timestamp())) {
                processFinishingM1Period(entry, currentMinute);
                return true;
            } else {
                return false;
            }
        });
    }

    protected void processFinishingM1Period(Map.Entry<SymbolTime, TrendBar> entry, Instant currentMinute) {
        inMemoryTrendBarStorage.saveTrendBar(entry.getValue());

        Instant currentHours = currentMinute.truncatedTo(HOURS);
        hourTBs.compute(new SymbolTime(entry.getKey().symbol(), entry.getValue().getTimestamp().truncatedTo(HOURS)), (k, tb) -> {
            if (tb != null) {
                return tb.updatePrices(entry.getValue(), H1);
            } else {
                return TrendBar.of(entry.getValue(), H1);
            }
        });

        boolean isNewHour = ChronoUnit.SECONDS.between(currentMinute.truncatedTo(HOURS)
                .plus(59, MINUTES).truncatedTo(HOURS), currentMinute) < 60;
        if (isNewHour) {
            hourTBs.entrySet().removeIf(e -> {
                if (entry.getKey().symbol().equals(e.getKey().symbol()) && currentHours.isAfter(e.getKey().timestamp())) {
                    processFinishingH1Period(e, currentHours);
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    private void processFinishingH1Period(Map.Entry<SymbolTime, TrendBar> entry, Instant currentHour) {
        inMemoryTrendBarStorage.saveTrendBar(entry.getValue());

        Instant currentDay = currentHour.truncatedTo(DAYS);
        dayTBs.compute(new SymbolTime(entry.getKey().symbol(), entry.getValue().getTimestamp().truncatedTo(DAYS)), (k, tb) -> {
            if (tb != null) {
                return tb.updatePrices(entry.getValue(), D1);
            } else {
                return TrendBar.of(entry.getValue(), D1);
            }
        });

        boolean isNewDay = ChronoUnit.SECONDS.between(currentHour.truncatedTo(DAYS)
                .plus(23, HOURS).truncatedTo(DAYS), currentHour) < 60;
        if (isNewDay) {
            dayTBs.entrySet().removeIf(e -> {
                if (entry.getKey().symbol().equals(e.getKey().symbol()) && currentDay.isAfter(e.getKey().timestamp())) {
                    inMemoryTrendBarStorage.saveTrendBar(e.getValue());
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

    @Override
    public void destroy() {
        scheduler.shutdownNow();
    }
}
