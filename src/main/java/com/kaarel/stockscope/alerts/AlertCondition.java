package com.kaarel.stockscope.alerts;

public enum AlertCondition {
    ABOVE,
    BELOW;

    public boolean matches(double currentPrice, double targetPrice) {
        return switch (this) {
            case ABOVE -> currentPrice >= targetPrice;
            case BELOW -> currentPrice <= targetPrice;
        };
    }
}
