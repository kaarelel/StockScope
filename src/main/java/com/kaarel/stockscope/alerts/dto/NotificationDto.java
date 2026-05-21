package com.kaarel.stockscope.alerts.dto;

import com.kaarel.stockscope.alerts.AlertCondition;

import java.time.Instant;

public record NotificationDto(
        Long id,
        Long alertId,
        String symbol,
        AlertCondition condition,
        double targetPrice,
        double triggeredPrice,
        String message,
        boolean read,
        Instant createdAt
) {}
