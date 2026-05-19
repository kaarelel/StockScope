package com.kaarel.stockscope.watchlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarel.stockscope.controller.GlobalExceptionHandler;
import com.kaarel.stockscope.session.SessionIdArgumentResolver;
import com.kaarel.stockscope.watchlist.dto.AddItemRequest;
import com.kaarel.stockscope.watchlist.dto.CreateWatchlistRequest;
import com.kaarel.stockscope.watchlist.dto.RenameWatchlistRequest;
import com.kaarel.stockscope.watchlist.dto.WatchlistDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WatchlistControllerTest {

    private WatchlistService service;
    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = Mockito.mock(WatchlistService.class);
        SessionIdArgumentResolver resolver = new SessionIdArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(new WatchlistController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(resolver)
                .build();
    }

    @Test
    void list_returns_empty_when_no_lists() throws Exception {
        when(service.findAllForSession(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void create_returns_201_with_location_header() throws Exception {
        WatchlistDto created = new WatchlistDto(7L, "Tech", Instant.now(), Instant.now(), List.of());
        when(service.create(anyString(), eq("Tech"))).thenReturn(created);

        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateWatchlistRequest("Tech"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/watchlist/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.name").value("Tech"));
    }

    @Test
    void create_with_blank_name_returns_400() throws Exception {
        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateWatchlistRequest("  "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_duplicate_name_returns_409() throws Exception {
        when(service.create(anyString(), eq("Tech")))
                .thenThrow(new DuplicateWatchlistNameException("Tech"));

        mockMvc.perform(post("/api/watchlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateWatchlistRequest("Tech"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void get_unknown_id_returns_404() throws Exception {
        when(service.findOne(anyString(), eq(99L)))
                .thenThrow(new WatchlistNotFoundException(99L));

        mockMvc.perform(get("/api/watchlist/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rename_returns_updated_dto() throws Exception {
        WatchlistDto updated = new WatchlistDto(1L, "Finance", Instant.now(), Instant.now(), List.of());
        when(service.rename(anyString(), eq(1L), eq("Finance"))).thenReturn(updated);

        mockMvc.perform(put("/api/watchlist/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new RenameWatchlistRequest("Finance"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Finance"));
    }

    @Test
    void delete_returns_204() throws Exception {
        doNothing().when(service).delete(anyString(), eq(1L));

        mockMvc.perform(delete("/api/watchlist/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_unknown_returns_404() throws Exception {
        doThrow(new WatchlistNotFoundException(99L)).when(service).delete(anyString(), eq(99L));

        mockMvc.perform(delete("/api/watchlist/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void add_item_returns_updated_list() throws Exception {
        WatchlistDto updated = new WatchlistDto(1L, "Tech", Instant.now(), Instant.now(), List.of("AAPL"));
        when(service.addItem(anyString(), eq(1L), eq("AAPL"))).thenReturn(updated);

        mockMvc.perform(put("/api/watchlist/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AddItemRequest("AAPL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbols[0]").value("AAPL"));
    }

    @Test
    void add_item_rejects_invalid_symbol() throws Exception {
        mockMvc.perform(put("/api/watchlist/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new AddItemRequest("bad symbol!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void remove_item_returns_updated_list() throws Exception {
        WatchlistDto updated = new WatchlistDto(1L, "Tech", Instant.now(), Instant.now(), List.of());
        when(service.removeItem(anyString(), eq(1L), eq("AAPL"))).thenReturn(updated);

        mockMvc.perform(delete("/api/watchlist/1/items/AAPL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbols").isEmpty());
    }
}
