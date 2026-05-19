package com.kaarel.stockscope.watchlist;

import com.kaarel.stockscope.model.RiskLevel;
import com.kaarel.stockscope.model.Stock;
import com.kaarel.stockscope.service.StockService;
import com.kaarel.stockscope.watchlist.dto.WatchlistDto;
import com.kaarel.stockscope.watchlist.dto.WatchlistItemView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository repository;

    @Mock
    private StockService stockService;

    @InjectMocks
    private WatchlistService service;

    @Test
    void create_trims_name_and_persists() {
        when(repository.existsBySessionIdAndName("s1", "Tech")).thenReturn(false);
        when(repository.save(any(Watchlist.class))).thenAnswer(inv -> inv.getArgument(0));

        WatchlistDto result = service.create("s1", "  Tech  ");

        assertThat(result.name()).isEqualTo("Tech");
        assertThat(result.symbols()).isEmpty();
    }

    @Test
    void create_throws_on_duplicate_name() {
        when(repository.existsBySessionIdAndName("s1", "Tech")).thenReturn(true);

        assertThatThrownBy(() -> service.create("s1", "Tech"))
                .isInstanceOf(DuplicateWatchlistNameException.class);
    }

    @Test
    void findOne_throws_when_session_does_not_own_it() {
        when(repository.findByIdAndSessionId(42L, "intruder")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findOne("intruder", 42L))
                .isInstanceOf(WatchlistNotFoundException.class);
    }

    @Test
    void rename_allows_same_name_for_same_watchlist() {
        Watchlist existing = new Watchlist("s1", "Tech");
        when(repository.findByIdAndSessionId(1L, "s1")).thenReturn(Optional.of(existing));

        WatchlistDto result = service.rename("s1", 1L, "Tech");

        assertThat(result.name()).isEqualTo("Tech");
    }

    @Test
    void rename_throws_when_renaming_to_another_existing_name() {
        Watchlist existing = new Watchlist("s1", "Tech");
        when(repository.findByIdAndSessionId(1L, "s1")).thenReturn(Optional.of(existing));
        when(repository.existsBySessionIdAndName("s1", "Finance")).thenReturn(true);

        assertThatThrownBy(() -> service.rename("s1", 1L, "Finance"))
                .isInstanceOf(DuplicateWatchlistNameException.class);
    }

    @Test
    void addItem_returns_updated_list() {
        Watchlist existing = new Watchlist("s1", "Tech");
        when(repository.findByIdAndSessionId(1L, "s1")).thenReturn(Optional.of(existing));

        WatchlistDto result = service.addItem("s1", 1L, "aapl");

        assertThat(result.symbols()).containsExactly("AAPL");
    }

    @Test
    void itemViewsForSession_returns_known_and_unknown_symbols() {
        Watchlist w1 = new Watchlist("s1", "Tech");
        w1.addItem("AAPL");
        w1.addItem("UNKNOWN");
        when(repository.findAllBySessionIdOrderByCreatedAtDesc("s1")).thenReturn(List.of(w1));
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock("AAPL", "Apple Inc.", "Technology", 200.0, 1.5)));
        when(stockService.findBySymbol("UNKNOWN")).thenReturn(Optional.empty());

        List<WatchlistItemView> views = service.itemViewsForSession("s1", 10);

        assertThat(views).hasSize(2);
        assertThat(views).extracting(WatchlistItemView::symbol).containsExactlyInAnyOrder("AAPL", "UNKNOWN");
        assertThat(views.stream().filter(v -> v.symbol().equals("AAPL")).findFirst().orElseThrow().known()).isTrue();
        assertThat(views.stream().filter(v -> v.symbol().equals("UNKNOWN")).findFirst().orElseThrow().known()).isFalse();
    }

    @Test
    void itemViewsForSession_deduplicates_across_watchlists_and_limits() {
        Watchlist a = new Watchlist("s1", "A"); a.addItem("AAPL");
        Watchlist b = new Watchlist("s1", "B"); b.addItem("AAPL"); b.addItem("MSFT");
        when(repository.findAllBySessionIdOrderByCreatedAtDesc("s1")).thenReturn(List.of(a, b));
        when(stockService.findBySymbol("AAPL")).thenReturn(Optional.of(stock("AAPL", "Apple", "Tech", 200, 1)));
        when(stockService.findBySymbol("MSFT")).thenReturn(Optional.of(stock("MSFT", "Microsoft", "Tech", 400, 2)));

        List<WatchlistItemView> views = service.itemViewsForSession("s1", 10);

        assertThat(views).hasSize(2);
    }

    private Stock stock(String symbol, String name, String sector, double price, double dayChangePct) {
        return new Stock(symbol, name, sector, price, price - 1, dayChangePct, 0, 0, 0, 1000, 10, 20, 0, RiskLevel.LOW, "");
    }
}
