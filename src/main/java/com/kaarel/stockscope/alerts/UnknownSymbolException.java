package com.kaarel.stockscope.alerts;

public class UnknownSymbolException extends RuntimeException {

    public UnknownSymbolException(String symbol) {
        super("Unknown symbol: " + symbol);
    }
}
