package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.model.RiskLevel;
import com.kaarel.stockscope.model.Stock;
import com.kaarel.stockscope.service.StockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
class AlertEvaluatorTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StockService stockService;

    private AlertEvaluator evaluator;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        evaluator = new AlertEvaluator(alertRepository, notificationService, stockService, fixedClock);
    }

    @Test
    void triggers_above_alert_when_price_crosses() {
        Alert alert = new Alert("s1", "AAPL", AlertCondition.ABOVE, 200.0);
        when(alertRepository.findAllByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(alert));
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock("AAPL", 210.0)));

        int triggered = evaluator.evaluateOnce();

        assertThat(triggered).isEqualTo(1);
        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(notificationService).recordTrigger(anyString(), any(Alert.class), anyDouble(), message.capture());
        assertThat(message.getValue()).contains("AAPL").contains("200.00").contains("210.00");
        assertThat(alert.isActive()).isFalse();
        assertThat(alert.getLastTriggeredAt()).isEqualTo(fixedClock.instant());
    }

    @Test
    void does_not_trigger_when_price_below_above_target() {
        Alert alert = new Alert("s1", "AAPL", AlertCondition.ABOVE, 300.0);
        when(alertRepository.findAllByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(alert));
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock("AAPL", 250.0)));

        int triggered = evaluator.evaluateOnce();

        assertThat(triggered).isZero();
        verify(notificationService, never()).recordTrigger(anyString(), any(), anyDouble(), anyString());
        assertThat(alert.isActive()).isTrue();
    }

    @Test
    void triggers_below_alert_when_price_drops() {
        Alert alert = new Alert("s1", "MSFT", AlertCondition.BELOW, 400.0);
        when(alertRepository.findAllByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(alert));
        when(stockService.findBySymbol("MSFT")).thenReturn(Optional.of(stock("MSFT", 395.0)));

        int triggered = evaluator.evaluateOnce();

        assertThat(triggered).isEqualTo(1);
        assertThat(alert.isActive()).isFalse();
    }

    @Test
    void deactivates_and_flushes_alert_before_recording_notification() {
        Alert alert = new Alert("s1", "AAPL", AlertCondition.ABOVE, 200.0);
        when(alertRepository.findAllByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(alert));
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock("AAPL", 210.0)));

        evaluator.evaluateOnce();

        InOrder ordered = inOrder(alertRepository, notificationService);
        ordered.verify(alertRepository).saveAndFlush(alert);
        ordered.verify(notificationService).recordTrigger(anyString(), any(Alert.class), anyDouble(), anyString());
        assertThat(alert.isActive()).isFalse();
    }

    @Test
    void skips_alerts_whose_symbol_is_unknown() {
        Alert alert = new Alert("s1", "NONE", AlertCondition.ABOVE, 1.0);
        when(alertRepository.findAllByActiveTrueOrderByCreatedAtAsc()).thenReturn(List.of(alert));
        when(stockService.findBySymbol("NONE")).thenReturn(Optional.empty());

        int triggered = evaluator.evaluateOnce();

        assertThat(triggered).isZero();
        assertThat(alert.isActive()).isTrue();
    }

    private Stock stock(String symbol, double price) {
        return new Stock(symbol, "Name", "Tech", price, price - 1, 1.0, 0, 0, 0, 100, 1, 10, 0, RiskLevel.LOW, "");
    }
}
