package com.kaarel.stockscope.watchlist;

public class DuplicateWatchlistNameException extends RuntimeException {

    public DuplicateWatchlistNameException(String name) {
        super("Watchlist with this name already exists: " + name);
    }
}
