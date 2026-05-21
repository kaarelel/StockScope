package com.kaarel.stockscope.alerts;

public class AlertNotFoundException extends RuntimeException {

    public AlertNotFoundException(Long id) {
        super("Alert not found: " + id);
    }
}
