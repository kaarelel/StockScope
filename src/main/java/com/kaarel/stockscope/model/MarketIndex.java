package com.kaarel.stockscope.model;

public record MarketIndex(
        String name,
        double value,
        double dayChangePct
) {}
