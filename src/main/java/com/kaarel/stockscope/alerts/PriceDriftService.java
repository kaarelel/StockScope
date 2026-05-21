package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.model.Stock;
import com.kaarel.stockscope.service.MockStockData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class PriceDriftService {

    static final double MAX_DRIFT_PCT = 0.015;
    static final double MIN_PRICE = 0.01;

    private final MockStockData stockData;
    private final Random random;

    @Autowired
    public PriceDriftService(MockStockData stockData) {
        this(stockData, new Random());
    }

    PriceDriftService(MockStockData stockData, Random random) {
        this.stockData = stockData;
        this.random = random;
    }

    public void driftOnce() {
        List<Stock> snapshot = stockData.all();
        for (Stock s : snapshot) {
            double driftPct = (random.nextDouble() * 2.0 - 1.0) * MAX_DRIFT_PCT;
            double newPrice = roundCents(Math.max(MIN_PRICE, s.price() * (1.0 + driftPct)));
            stockData.updatePrice(s.symbol(), newPrice);
        }
    }

    private double roundCents(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
