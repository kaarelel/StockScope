package com.kaarel.stockscope.watchlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = @UniqueConstraint(name = "uk_watchlist_items_list_symbol", columnNames = {"watchlist_id", "symbol"}))
@EntityListeners(AuditingEntityListener.class)
public class WatchlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "watchlist_id", nullable = false)
    private Watchlist watchlist;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private Instant addedAt;

    protected WatchlistItem() {
    }

    public WatchlistItem(Watchlist watchlist, String symbol) {
        this.watchlist = watchlist;
        this.symbol = symbol;
    }

    public Long getId() {
        return id;
    }

    public Watchlist getWatchlist() {
        return watchlist;
    }

    public String getSymbol() {
        return symbol;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WatchlistItem that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
