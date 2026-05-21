package com.kaarel.stockscope.alerts.dto;

import com.kaarel.stockscope.alerts.AlertCondition;

import java.time.Instant;

public record AlertDto(
        Long id,
        String symbol,
        AlertCondition condition,
        double targetPrice,
        boolean active,
        Instant createdAt,
        Instant lastTriggeredAt
) {}
