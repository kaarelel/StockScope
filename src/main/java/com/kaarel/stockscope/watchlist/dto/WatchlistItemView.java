package com.kaarel.stockscope.watchlist.dto;

public record WatchlistItemView(
        String symbol,
        String name,
        String sector,
        double price,
        double dayChangePct,
        boolean known
) {}
