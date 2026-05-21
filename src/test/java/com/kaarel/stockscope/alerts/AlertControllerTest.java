package com.kaarel.stockscope.alerts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarel.stockscope.alerts.dto.AlertDto;
import com.kaarel.stockscope.alerts.dto.CreateAlertRequest;
import com.kaarel.stockscope.controller.GlobalExceptionHandler;
import com.kaarel.stockscope.session.SessionIdArgumentResolver;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AlertControllerTest {

    private AlertService service;
    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        service = Mockito.mock(AlertService.class);
        SessionIdArgumentResolver resolver = new SessionIdArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(new AlertController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(resolver)
                .build();
    }

    @Test
    void list_returns_empty_when_no_alerts() throws Exception {
        when(service.findAllForSession(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void create_returns_201_with_location_header() throws Exception {
        AlertDto created = new AlertDto(7L, "AAPL", AlertCondition.ABOVE, 250.0, true, Instant.now(), null);
        when(service.create(anyString(), any(CreateAlertRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new CreateAlertRequest("AAPL", AlertCondition.ABOVE, 250.0))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/api/alerts/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.symbol").value("AAPL"))
                .andExpect(jsonPath("$.condition").value("ABOVE"));
    }

    @Test
    void create_with_invalid_symbol_returns_400() throws Exception {
        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"bad symbol!\",\"condition\":\"ABOVE\",\"targetPrice\":1.0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_with_non_positive_target_returns_400() throws Exception {
        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"AAPL\",\"condition\":\"ABOVE\",\"targetPrice\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_unknown_symbol_returns_400() throws Exception {
        when(service.create(anyString(), any(CreateAlertRequest.class)))
                .thenThrow(new UnknownSymbolException("ZZZZ"));

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"ZZZZ\",\"condition\":\"ABOVE\",\"targetPrice\":1.0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void delete_returns_204() throws Exception {
        doNothing().when(service).delete(anyString(), eq(1L));

        mockMvc.perform(delete("/api/alerts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_unknown_returns_404() throws Exception {
        doThrow(new AlertNotFoundException(99L)).when(service).delete(anyString(), eq(99L));

        mockMvc.perform(delete("/api/alerts/99"))
                .andExpect(status().isNotFound());
    }
}
