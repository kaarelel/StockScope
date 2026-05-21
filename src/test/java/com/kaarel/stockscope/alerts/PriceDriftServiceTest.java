package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.service.MockStockData;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class PriceDriftServiceTest {

    private final MockStockData data = new MockStockData();

    @Test
    void drift_keeps_price_within_bounds() {
        PriceDriftService drift = new PriceDriftService(data, new Random(42L));
        double before = priceOf("AAPL");

        drift.driftOnce();

        double after = priceOf("AAPL");
        double pctDelta = Math.abs(after - before) / before;
        assertThat(pctDelta).isLessThanOrEqualTo(PriceDriftService.MAX_DRIFT_PCT + 1e-6);
        assertThat(after).isGreaterThan(0.0);
    }

    @Test
    void drift_eventually_changes_price() {
        PriceDriftService drift = new PriceDriftService(data, new Random(1L));
        double before = priceOf("AAPL");

        for (int i = 0; i < 5; i++) {
            drift.driftOnce();
        }

        assertThat(priceOf("AAPL")).isNotEqualTo(before);
    }

    private double priceOf(String symbol) {
        return data.all().stream()
                .filter(s -> s.symbol().equals(symbol))
                .findFirst()
                .orElseThrow()
                .price();
    }
}
