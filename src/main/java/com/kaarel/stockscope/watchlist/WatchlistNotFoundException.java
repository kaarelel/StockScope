package com.kaarel.stockscope.watchlist;

public class WatchlistNotFoundException extends RuntimeException {

    public WatchlistNotFoundException(Long id) {
        super("Watchlist not found: " + id);
    }
}
