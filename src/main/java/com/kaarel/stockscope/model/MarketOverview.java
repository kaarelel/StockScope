package com.kaarel.stockscope.model;

import java.util.List;

public record MarketOverview(
        String summary,
        String sentiment,
        List<MarketIndex> indices,
        List<Stock> topGainers,
        List<Stock> topLosers,
        List<Stock> mostActive
) {}
