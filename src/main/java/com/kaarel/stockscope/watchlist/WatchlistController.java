package com.kaarel.stockscope.watchlist;

import com.kaarel.stockscope.session.SessionId;
import com.kaarel.stockscope.watchlist.dto.AddItemRequest;
import com.kaarel.stockscope.watchlist.dto.CreateWatchlistRequest;
import com.kaarel.stockscope.watchlist.dto.RenameWatchlistRequest;
import com.kaarel.stockscope.watchlist.dto.WatchlistDto;
import com.kaarel.stockscope.watchlist.dto.WatchlistItemView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService service;

    public WatchlistController(WatchlistService service) {
        this.service = service;
    }

    @GetMapping
    public List<WatchlistDto> list(@SessionId String sessionId) {
        return service.findAllForSession(sessionId);
    }

    @GetMapping("/{id}")
    public WatchlistDto get(@SessionId String sessionId, @PathVariable Long id) {
        return service.findOne(sessionId, id);
    }

    @PostMapping
    public ResponseEntity<WatchlistDto> create(
            @SessionId String sessionId,
            @Valid @RequestBody CreateWatchlistRequest request,
            UriComponentsBuilder uriBuilder) {
        WatchlistDto created = service.create(sessionId, request.name());
        URI location = uriBuilder.path("/api/watchlist/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public WatchlistDto rename(
            @SessionId String sessionId,
            @PathVariable Long id,
            @Valid @RequestBody RenameWatchlistRequest request) {
        return service.rename(sessionId, id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@SessionId String sessionId, @PathVariable Long id) {
        service.delete(sessionId, id);
    }

    @PutMapping("/{id}/items")
    public WatchlistDto addItem(
            @SessionId String sessionId,
            @PathVariable Long id,
            @Valid @RequestBody AddItemRequest request) {
        return service.addItem(sessionId, id, request.symbol());
    }

    @DeleteMapping("/{id}/items/{symbol}")
    public WatchlistDto removeItem(
            @SessionId String sessionId,
            @PathVariable Long id,
            @PathVariable String symbol) {
        return service.removeItem(sessionId, id, symbol);
    }

    @GetMapping("/items")
    public List<WatchlistItemView> aggregatedItems(
            @SessionId String sessionId,
            @RequestParam(defaultValue = "5") int limit) {
        return service.itemViewsForSession(sessionId, limit);
    }
}
