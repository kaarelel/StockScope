package com.kaarel.stockscope.watchlist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    List<Watchlist> findAllBySessionIdOrderByCreatedAtDesc(String sessionId);

    Optional<Watchlist> findByIdAndSessionId(Long id, String sessionId);

    boolean existsBySessionIdAndName(String sessionId, String name);
}
