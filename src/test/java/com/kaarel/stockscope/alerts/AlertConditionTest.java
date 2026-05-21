package com.kaarel.stockscope.alerts;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlertConditionTest {

    @Test
    void above_matches_when_price_at_or_over_target() {
        assertThat(AlertCondition.ABOVE.matches(101.0, 100.0)).isTrue();
        assertThat(AlertCondition.ABOVE.matches(100.0, 100.0)).isTrue();
        assertThat(AlertCondition.ABOVE.matches(99.99, 100.0)).isFalse();
    }

    @Test
    void below_matches_when_price_at_or_under_target() {
        assertThat(AlertCondition.BELOW.matches(99.0, 100.0)).isTrue();
        assertThat(AlertCondition.BELOW.matches(100.0, 100.0)).isTrue();
        assertThat(AlertCondition.BELOW.matches(100.01, 100.0)).isFalse();
    }
}
