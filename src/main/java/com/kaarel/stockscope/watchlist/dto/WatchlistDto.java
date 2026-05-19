package com.kaarel.stockscope.watchlist.dto;

import java.time.Instant;
import java.util.List;

public record WatchlistDto(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        List<String> symbols
) {}
