package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.NotificationDto;
import com.kaarel.stockscope.controller.GlobalExceptionHandler;
import com.kaarel.stockscope.session.SessionIdArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest {

    private NotificationService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = Mockito.mock(NotificationService.class);
        SessionIdArgumentResolver resolver = new SessionIdArgumentResolver();
        mockMvc = MockMvcBuilders.standaloneSetup(new NotificationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(resolver)
                .build();
    }

    @Test
    void list_returns_dtos() throws Exception {
        NotificationDto dto = new NotificationDto(1L, 11L, "AAPL", AlertCondition.ABOVE, 200.0, 210.0, "msg", false, Instant.now());
        when(service.findForSession(anyString(), eq(false))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void list_passes_unread_param_to_service() throws Exception {
        when(service.findForSession(anyString(), eq(true))).thenReturn(List.of());

        mockMvc.perform(get("/api/notifications?unread=true"))
                .andExpect(status().isOk());
    }

    @Test
    void unread_count_returns_long() throws Exception {
        when(service.unreadCount(anyString())).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void patch_marks_read() throws Exception {
        NotificationDto dto = new NotificationDto(5L, 11L, "AAPL", AlertCondition.ABOVE, 200.0, 210.0, "msg", true, Instant.now());
        when(service.markRead(anyString(), eq(5L))).thenReturn(dto);

        mockMvc.perform(patch("/api/notifications/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    void patch_unknown_returns_404() throws Exception {
        when(service.markRead(anyString(), eq(99L)))
                .thenThrow(new NotificationNotFoundException(99L));

        mockMvc.perform(patch("/api/notifications/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void read_all_returns_count() throws Exception {
        when(service.markAllRead(anyString())).thenReturn(4);

        mockMvc.perform(post("/api/notifications/read-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(4));
    }
}
