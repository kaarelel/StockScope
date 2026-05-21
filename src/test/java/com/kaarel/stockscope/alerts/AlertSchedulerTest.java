package com.kaarel.stockscope.alerts;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlertSchedulerTest {

    @Mock
    private PriceDriftService priceDriftService;

    @Mock
    private AlertEvaluator alertEvaluator;

    @InjectMocks
    private AlertScheduler scheduler;

    @Test
    void tick_drifts_prices_then_evaluates() {
        scheduler.tick();

        InOrder ordered = inOrder(priceDriftService, alertEvaluator);
        ordered.verify(priceDriftService).driftOnce();
        ordered.verify(alertEvaluator).evaluateOnce();
    }

    @Test
    void tick_swallows_runtime_exception_from_drift_and_skips_evaluator() {
        doThrow(new IllegalStateException("boom")).when(priceDriftService).driftOnce();

        scheduler.tick();

        verify(alertEvaluator, never()).evaluateOnce();
    }

    @Test
    void tick_swallows_runtime_exception_from_evaluator() {
        doThrow(new IllegalStateException("boom")).when(alertEvaluator).evaluateOnce();

        scheduler.tick();

        verify(priceDriftService).driftOnce();
        verify(alertEvaluator).evaluateOnce();
    }
}
