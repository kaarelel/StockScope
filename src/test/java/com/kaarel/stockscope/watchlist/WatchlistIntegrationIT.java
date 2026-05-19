package com.kaarel.stockscope.watchlist;

import com.fasterxml.jackson.databind.JsonNode;
import com.kaarel.stockscope.session.SessionIdHolder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WatchlistIntegrationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper json;

    @Test
    void happy_path_create_add_remove_persists() throws Exception {
        MvcResult firstHit = mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andReturn();
        Cookie sessionCookie = firstHit.getResponse().getCookie(SessionIdHolder.COOKIE_NAME);
        assertThat(sessionCookie).isNotNull();

        MvcResult created = mockMvc.perform(post("/api/watchlist")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Tech\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();
        Long id = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/watchlist/" + id + "/items")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"AAPL\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/watchlist/" + id + "/items")
                        .cookie(sessionCookie)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"aapl\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbols.length()").value(1));

        mockMvc.perform(get("/api/watchlist").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbols[0]").value("AAPL"));

        mockMvc.perform(delete("/api/watchlist/" + id + "/items/AAPL").cookie(sessionCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbols").isEmpty());
    }

    @Test
    void two_sessions_are_isolated() throws Exception {
        Cookie session1 = obtainSession();
        Cookie session2 = obtainSession();
        assertThat(session1.getValue()).isNotEqualTo(session2.getValue());

        mockMvc.perform(post("/api/watchlist")
                        .cookie(session1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Session 1 list\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/watchlist").cookie(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/watchlist").cookie(session2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void duplicate_name_returns_409() throws Exception {
        Cookie session = obtainSession();

        mockMvc.perform(post("/api/watchlist")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dup\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/watchlist")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Dup\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void cross_session_get_returns_404() throws Exception {
        Cookie session1 = obtainSession();
        Cookie session2 = obtainSession();

        MvcResult created = mockMvc.perform(post("/api/watchlist")
                        .cookie(session1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Owned by 1\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        Long id = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/watchlist/" + id).cookie(session2))
                .andExpect(status().isNotFound());
    }

    @Test
    void aggregated_items_endpoint_returns_only_known_and_unknown() throws Exception {
        Cookie session = obtainSession();

        MvcResult created = mockMvc.perform(post("/api/watchlist")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mixed\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode createdNode = json.readTree(created.getResponse().getContentAsString());
        long id = createdNode.get("id").asLong();

        mockMvc.perform(put("/api/watchlist/" + id + "/items")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\":\"AAPL\"}"));
        mockMvc.perform(put("/api/watchlist/" + id + "/items")
                .cookie(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"symbol\":\"NOPE\"}"));

        mockMvc.perform(get("/api/watchlist/items?limit=10").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    private Cookie obtainSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/watchlist"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(SessionIdHolder.COOKIE_NAME);
    }
}
