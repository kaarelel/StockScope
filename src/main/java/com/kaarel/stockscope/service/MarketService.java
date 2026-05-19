package com.kaarel.stockscope.service;

import com.kaarel.stockscope.model.MarketIndex;
import com.kaarel.stockscope.model.MarketOverview;
import com.kaarel.stockscope.model.Stock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MarketService {

    private final StockService stockService;

    public MarketService(StockService stockService) {
        this.stockService = stockService;
    }

    public MarketOverview overview() {
        List<Stock> gainers = stockService.topGainers(5);
        List<Stock> losers = stockService.topLosers(5);
        List<Stock> active = stockService.mostActive(5);

        double avgChange = stockService.search(null, null, "symbol", "asc").stream()
                .mapToDouble(Stock::dayChangePct)
                .average()
                .orElse(0);

        String sentiment = sentimentLabel(avgChange);
        String summary = buildSummary(avgChange, gainers, losers);

        List<MarketIndex> indices = List.of(
                new MarketIndex("S&P 500", 5842.42, 0.42),
                new MarketIndex("Nasdaq 100", 20518.31, 0.85),
                new MarketIndex("Dow Jones", 42188.18, 0.18),
                new MarketIndex("Russell 2000", 2342.85, -0.32)
        );

        return new MarketOverview(summary, sentiment, indices, gainers, losers, active);
    }

    private String sentimentLabel(double avgChange) {
        if (avgChange > 1.0) return "Tugevalt positiivne";
        if (avgChange > 0.2) return "Positiivne";
        if (avgChange > -0.2) return "Neutraalne";
        if (avgChange > -1.0) return "Negatiivne";
        return "Tugevalt negatiivne";
    }

    private String buildSummary(double avgChange, List<Stock> gainers, List<Stock> losers) {
        String trend = avgChange >= 0 ? "tõusis" : "langes";
        String topGainer = gainers.isEmpty() ? "" : gainers.get(0).symbol();
        String topLoser = losers.isEmpty() ? "" : losers.get(0).symbol();
        return String.format(
                "Turu keskmine %s täna %.2f%%. Kõige rohkem tõusis %s, kõige rohkem langes %s.",
                trend, Math.abs(avgChange), topGainer, topLoser);
    }
}
