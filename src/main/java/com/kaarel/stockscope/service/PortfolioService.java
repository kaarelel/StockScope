package com.kaarel.stockscope.service;

import com.kaarel.stockscope.model.PortfolioRequest;
import com.kaarel.stockscope.model.PortfolioResponse;
import com.kaarel.stockscope.model.RiskLevel;
import com.kaarel.stockscope.model.Stock;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PortfolioService {

    private final StockService stockService;

    public PortfolioService(StockService stockService) {
        this.stockService = stockService;
    }

    public PortfolioResponse simulate(PortfolioRequest request) {
        double totalWeight = request.allocations().stream()
                .mapToDouble(PortfolioRequest.Allocation::weightPct)
                .sum();
        if (totalWeight <= 0) {
            throw new IllegalArgumentException("Kogu kaal peab olema > 0");
        }

        double invested = request.totalAmount();
        List<PortfolioResponse.Holding> holdings = new ArrayList<>();
        Map<String, Double> sectorAllocation = new HashMap<>();
        double currentValue = 0;
        double weightedRiskScore = 0;

        for (var alloc : request.allocations()) {
            Stock stock = stockService.findBySymbol(alloc.symbol())
                    .orElseThrow(() -> new IllegalArgumentException("Aktsia ei leitud: " + alloc.symbol()));

            double normalizedWeight = alloc.weightPct() / totalWeight;
            double allocated = invested * normalizedWeight;
            double shares = allocated / stock.previousClose();
            double current = shares * stock.price();
            double returnPct = ((current - allocated) / allocated) * 100.0;

            currentValue += current;
            weightedRiskScore += normalizedWeight * riskScore(stock.risk());

            sectorAllocation.merge(stock.sector(), normalizedWeight * 100.0, Double::sum);

            holdings.add(new PortfolioResponse.Holding(
                    stock.symbol(),
                    stock.name(),
                    stock.sector(),
                    normalizedWeight * 100.0,
                    allocated,
                    shares,
                    current,
                    returnPct
            ));
        }

        double absoluteReturn = currentValue - invested;
        double returnPct = (absoluteReturn / invested) * 100.0;
        RiskLevel overallRisk = riskFromScore(weightedRiskScore);
        String commentary = buildCommentary(returnPct, overallRisk, sectorAllocation);

        return new PortfolioResponse(
                invested, currentValue, absoluteReturn, returnPct,
                holdings, sectorAllocation, overallRisk, commentary);
    }

    private double riskScore(RiskLevel risk) {
        return switch (risk) {
            case LOW -> 1.0;
            case MEDIUM -> 2.0;
            case HIGH -> 3.0;
        };
    }

    private RiskLevel riskFromScore(double score) {
        if (score < 1.5) return RiskLevel.LOW;
        if (score < 2.5) return RiskLevel.MEDIUM;
        return RiskLevel.HIGH;
    }

    private String buildCommentary(double returnPct, RiskLevel risk, Map<String, Double> sectors) {
        StringBuilder sb = new StringBuilder();
        if (returnPct >= 0) {
            sb.append(String.format("Päevane tootlus on %+.2f%%, mis on positiivne. ", returnPct));
        } else {
            sb.append(String.format("Päevane tootlus on %+.2f%%, mis on negatiivne. ", returnPct));
        }
        sb.append("Üldine riskitase: ").append(switch (risk) {
            case LOW -> "madal";
            case MEDIUM -> "keskmine";
            case HIGH -> "kõrge";
        }).append(". ");

        String topSector = sectors.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");
        double topShare = sectors.getOrDefault(topSector, 0.0);
        sb.append(String.format("Suurim sektor: %s (%.1f%%). ", topSector, topShare));
        if (sectors.size() < 3) {
            sb.append("Kaaluge hajutamist rohkemate sektorite vahel.");
        } else {
            sb.append("Hajutus sektorite vahel on hea.");
        }
        return sb.toString();
    }
}
