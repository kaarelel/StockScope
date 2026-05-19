package com.kaarel.stockscope.model;

public record Stock(
        String symbol,
        String name,
        String sector,
        double price,
        double previousClose,
        double dayChangePct,
        double weekChangePct,
        double monthChangePct,
        double yearChangePct,
        long marketCapBillions,
        long volumeMillions,
        double peRatio,
        double dividendYield,
        RiskLevel risk,
        String summary
) {
    public double dayChangeAbsolute() {
        return price - previousClose;
    }
}
