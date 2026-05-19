package com.kaarel.stockscope.model;

import java.util.List;
import java.util.Map;

public record PortfolioResponse(
        double invested,
        double currentValue,
        double absoluteReturn,
        double returnPct,
        List<Holding> holdings,
        Map<String, Double> sectorAllocation,
        RiskLevel overallRisk,
        String commentary
) {
    public record Holding(
            String symbol,
            String name,
            String sector,
            double weightPct,
            double allocatedAmount,
            double shares,
            double currentValue,
            double returnPct
    ) {}
}
