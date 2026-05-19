package com.kaarel.stockscope.watchlist;

import com.kaarel.stockscope.model.Stock;
import com.kaarel.stockscope.service.StockService;
import com.kaarel.stockscope.watchlist.dto.WatchlistDto;
import com.kaarel.stockscope.watchlist.dto.WatchlistItemView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class WatchlistService {

    private final WatchlistRepository repository;
    private final StockService stockService;

    public WatchlistService(WatchlistRepository repository, StockService stockService) {
        this.repository = repository;
        this.stockService = stockService;
    }

    @Transactional(readOnly = true)
    public List<WatchlistDto> findAllForSession(String sessionId) {
        return repository.findAllBySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public WatchlistDto findOne(String sessionId, Long id) {
        return toDto(load(sessionId, id));
    }

    public WatchlistDto create(String sessionId, String name) {
        String trimmed = name.trim();
        if (repository.existsBySessionIdAndName(sessionId, trimmed)) {
            throw new DuplicateWatchlistNameException(trimmed);
        }
        Watchlist saved = repository.save(new Watchlist(sessionId, trimmed));
        return toDto(saved);
    }

    public WatchlistDto rename(String sessionId, Long id, String newName) {
        Watchlist watchlist = load(sessionId, id);
        String trimmed = newName.trim();
        if (!watchlist.getName().equals(trimmed) && repository.existsBySessionIdAndName(sessionId, trimmed)) {
            throw new DuplicateWatchlistNameException(trimmed);
        }
        watchlist.setName(trimmed);
        return toDto(watchlist);
    }

    public void delete(String sessionId, Long id) {
        Watchlist watchlist = load(sessionId, id);
        repository.delete(watchlist);
    }

    public WatchlistDto addItem(String sessionId, Long id, String symbol) {
        Watchlist watchlist = load(sessionId, id);
        watchlist.addItem(symbol);
        return toDto(watchlist);
    }

    public WatchlistDto removeItem(String sessionId, Long id, String symbol) {
        Watchlist watchlist = load(sessionId, id);
        watchlist.removeItem(symbol);
        return toDto(watchlist);
    }

    @Transactional(readOnly = true)
    public List<WatchlistItemView> itemViewsForSession(String sessionId, int limit) {
        return repository.findAllBySessionIdOrderByCreatedAtDesc(sessionId).stream()
                .flatMap(w -> w.getItems().stream())
                .map(WatchlistItem::getSymbol)
                .distinct()
                .map(this::toItemView)
                .sorted(Comparator.comparingDouble(WatchlistItemView::dayChangePct).reversed())
                .limit(limit)
                .toList();
    }

    private Watchlist load(String sessionId, Long id) {
        return repository.findByIdAndSessionId(id, sessionId)
                .orElseThrow(() -> new WatchlistNotFoundException(id));
    }

    private WatchlistDto toDto(Watchlist w) {
        List<String> symbols = w.getItems().stream()
                .map(WatchlistItem::getSymbol)
                .sorted()
                .toList();
        return new WatchlistDto(w.getId(), w.getName(), w.getCreatedAt(), w.getUpdatedAt(), symbols);
    }

    private WatchlistItemView toItemView(String symbol) {
        return stockService.findBySymbol(symbol)
                .map(this::knownItem)
                .orElseGet(() -> new WatchlistItemView(symbol, symbol, "—", 0.0, 0.0, false));
    }

    private WatchlistItemView knownItem(Stock s) {
        return new WatchlistItemView(s.symbol(), s.name(), s.sector(), s.price(), s.dayChangePct(), true);
    }
}
