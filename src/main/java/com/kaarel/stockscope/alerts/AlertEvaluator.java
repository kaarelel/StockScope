package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Component
public class AlertEvaluator {

    private static final Logger log = LoggerFactory.getLogger(AlertEvaluator.class);

    private final AlertRepository alertRepository;
    private final NotificationService notificationService;
    private final StockService stockService;
    private final Clock clock;

    public AlertEvaluator(AlertRepository alertRepository,
                          NotificationService notificationService,
                          StockService stockService,
                          Clock clock) {
        this.alertRepository = alertRepository;
        this.notificationService = notificationService;
        this.stockService = stockService;
        this.clock = clock;
    }

    @Transactional
    public int evaluateOnce() {
        List<Alert> active = alertRepository.findAllByActiveTrueOrderByCreatedAtAsc();
        int triggered = 0;
        for (Alert alert : active) {
            if (tryTrigger(alert)) {
                triggered++;
            }
        }
        if (triggered > 0) {
            log.info("Alert evaluator triggered {} alerts", triggered);
        }
        return triggered;
    }

    private boolean tryTrigger(Alert alert) {
        return stockService.findBySymbol(alert.getSymbol())
                .map(stock -> {
                    double currentPrice = stock.price();
                    if (!alert.getCondition().matches(currentPrice, alert.getTargetPrice())) {
                        return false;
                    }
                    // Persist deactivation FIRST so a partial-failure retry cannot create a second
                    // notification for the same alert. The DB-level unique constraint on
                    // notifications(alert_id) is the belt-and-braces guard.
                    alert.markTriggered(Instant.now(clock));
                    alert.deactivate();
                    alertRepository.saveAndFlush(alert);
                    String message = formatMessage(alert, currentPrice);
                    notificationService.recordTrigger(alert.getSessionId(), alert, currentPrice, message);
                    return true;
                })
                .orElse(false);
    }

    private String formatMessage(Alert alert, double currentPrice) {
        return String.format(
                Locale.ROOT,
                "%s is %s target %.2f (now %.2f)",
                alert.getSymbol(),
                alert.getCondition() == AlertCondition.ABOVE ? "at or above" : "at or below",
                alert.getTargetPrice(),
                currentPrice);
    }
}
