package com.kaarel.stockscope.watchlist;

import com.kaarel.stockscope.config.JpaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
@ActiveProfiles("test")
class WatchlistRepositoryTest {

    @Autowired
    private WatchlistRepository repository;

    @Test
    void saves_watchlist_with_auditing_fields() {
        Watchlist saved = repository.save(new Watchlist("session-1", "Tech picks"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void finds_all_by_session_id_in_descending_created_order() {
        repository.save(new Watchlist("session-1", "First"));
        repository.save(new Watchlist("session-1", "Second"));
        repository.save(new Watchlist("session-2", "Other session"));

        List<Watchlist> result = repository.findAllBySessionIdOrderByCreatedAtDesc("session-1");

        assertThat(result).hasSize(2).extracting(Watchlist::getName).containsExactly("Second", "First");
    }

    @Test
    void finds_by_id_only_for_owning_session() {
        Watchlist saved = repository.save(new Watchlist("owner", "My list"));

        Optional<Watchlist> owned = repository.findByIdAndSessionId(saved.getId(), "owner");
        Optional<Watchlist> intruder = repository.findByIdAndSessionId(saved.getId(), "intruder");

        assertThat(owned).isPresent();
        assertThat(intruder).isEmpty();
    }

    @Test
    void rejects_duplicate_name_per_session() {
        repository.save(new Watchlist("session-1", "Tech"));

        boolean exists = repository.existsBySessionIdAndName("session-1", "Tech");
        boolean otherSession = repository.existsBySessionIdAndName("session-2", "Tech");

        assertThat(exists).isTrue();
        assertThat(otherSession).isFalse();
    }

    @Test
    void cascades_items_on_watchlist_save_and_delete() {
        Watchlist list = new Watchlist("session-1", "Tech");
        list.addItem("AAPL");
        list.addItem("NVDA");
        Watchlist saved = repository.save(list);

        assertThat(saved.getItems()).hasSize(2);

        repository.delete(saved);
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void add_item_is_idempotent_case_insensitive() {
        Watchlist list = new Watchlist("session-1", "Tech");

        assertThat(list.addItem("AAPL")).isTrue();
        assertThat(list.addItem("aapl")).isFalse();
        assertThat(list.getItems()).hasSize(1);
    }

    @Test
    void remove_item_returns_false_if_missing() {
        Watchlist list = new Watchlist("session-1", "Tech");
        list.addItem("AAPL");

        assertThat(list.removeItem("MSFT")).isFalse();
        assertThat(list.removeItem("aapl")).isTrue();
        assertThat(list.getItems()).isEmpty();
    }
}
