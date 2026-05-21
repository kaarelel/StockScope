package com.kaarel.stockscope.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "stockscope.alerts.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(AlertScheduler.class);

    private final PriceDriftService priceDriftService;
    private final AlertEvaluator alertEvaluator;

    public AlertScheduler(PriceDriftService priceDriftService, AlertEvaluator alertEvaluator) {
        this.priceDriftService = priceDriftService;
        this.alertEvaluator = alertEvaluator;
    }

    @Scheduled(fixedDelayString = "${stockscope.alerts.scheduler.delay-ms:30000}",
            initialDelayString = "${stockscope.alerts.scheduler.initial-delay-ms:30000}")
    public void tick() {
        try {
            priceDriftService.driftOnce();
            alertEvaluator.evaluateOnce();
        } catch (RuntimeException e) {
            log.warn("Alert scheduler tick failed", e);
        }
    }
}
