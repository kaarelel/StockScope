package com.kaarel.stockscope.alerts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarel.stockscope.service.MockStockData;
import com.kaarel.stockscope.session.SessionIdHolder;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = "stockscope.alerts.scheduler.enabled=false")
class AlertsIntegrationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private AlertEvaluator alertEvaluator;

    @Autowired
    private MockStockData stockData;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void resetState() {
        notificationRepository.deleteAll();
        alertRepository.deleteAll();
        stockData.updatePrice("AAPL", 232.41);
        stockData.updatePrice("MSFT", 421.83);
    }

    @Test
    void create_alert_returns_201_and_listed_for_session() throws Exception {
        Cookie session = obtainSession();

        mockMvc.perform(post("/api/alerts")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"AAPL\",\"condition\":\"ABOVE\",\"targetPrice\":300}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.symbol").value("AAPL"));

        mockMvc.perform(get("/api/alerts").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void alerts_isolated_between_sessions() throws Exception {
        Cookie session1 = obtainSession();
        Cookie session2 = obtainSession();
        assertThat(session1.getValue()).isNotEqualTo(session2.getValue());

        mockMvc.perform(post("/api/alerts")
                        .cookie(session1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symbol\":\"AAPL\",\"condition\":\"ABOVE\",\"targetPrice\":300}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/alerts").cookie(session1))
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/alerts").cookie(session2))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void evaluator_triggers_alert_and_creates_notification_visible_only_to_owner() throws Exception {
        Cookie session1 = obtainSession();
        Cookie session2 = obtainSession();

        long alertId = createAlert(session1, "AAPL", "BELOW", 250.0);
        stockData.updatePrice("AAPL", 240.0);

        alertEvaluator.evaluateOnce();

        mockMvc.perform(get("/api/notifications").cookie(session1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].symbol").value("AAPL"))
                .andExpect(jsonPath("$[0].read").value(false))
                .andExpect(jsonPath("$[0].alertId").value((int) alertId));

        mockMvc.perform(get("/api/notifications").cookie(session2))
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/notifications/unread-count").cookie(session1))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void mark_read_and_bulk_read_work_per_session() throws Exception {
        Cookie session = obtainSession();
        long alertId = createAlert(session, "AAPL", "BELOW", 250.0);
        stockData.updatePrice("AAPL", 240.0);
        alertEvaluator.evaluateOnce();

        long notificationId = firstNotificationId(session);

        mockMvc.perform(patch("/api/notifications/" + notificationId).cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(get("/api/notifications?unread=true").cookie(session))
                .andExpect(jsonPath("$.length()").value(0));

        createAlertAndTrigger(session, "MSFT", "BELOW", 500.0, 400.0);
        mockMvc.perform(post("/api/notifications/read-all").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.updated").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        mockMvc.perform(get("/api/notifications/unread-count").cookie(session))
                .andExpect(jsonPath("$.count").value(0));

        assertThat(alertId).isPositive();
    }

    @Test
    void notification_count_capped_at_100_per_session() throws Exception {
        Cookie session = obtainSession();

        for (int i = 0; i < 110; i++) {
            long alertId = createAlert(session, "AAPL", "BELOW", 1000.0);
            stockData.updatePrice("AAPL", 100.0);
            alertEvaluator.evaluateOnce();
            assertThat(alertId).isPositive();
        }

        long count = notificationRepository.countBySessionId(session.getValue());
        assertThat(count).isEqualTo(100);
    }

    @Test
    void cross_session_patch_returns_404() throws Exception {
        Cookie session1 = obtainSession();
        Cookie session2 = obtainSession();

        createAlertAndTrigger(session1, "AAPL", "BELOW", 250.0, 240.0);
        long notificationId = firstNotificationId(session1);

        mockMvc.perform(patch("/api/notifications/" + notificationId).cookie(session2))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_alert_succeeds_for_owner_and_404_for_stranger() throws Exception {
        Cookie session1 = obtainSession();
        Cookie session2 = obtainSession();
        long alertId = createAlert(session1, "AAPL", "ABOVE", 300.0);

        mockMvc.perform(delete("/api/alerts/" + alertId).cookie(session2))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/alerts/" + alertId).cookie(session1))
                .andExpect(status().isNoContent());
    }

    private long createAlert(Cookie session, String symbol, String condition, double target) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/alerts")
                        .cookie(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"symbol\":\"%s\",\"condition\":\"%s\",\"targetPrice\":%s}",
                                symbol, condition, target)))
                .andExpect(status().isCreated())
                .andReturn();
        return json.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }

    private void createAlertAndTrigger(Cookie session, String symbol, String condition, double target, double currentPrice) throws Exception {
        createAlert(session, symbol, condition, target);
        stockData.updatePrice(symbol, currentPrice);
        alertEvaluator.evaluateOnce();
    }

    private long firstNotificationId(Cookie session) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/notifications").cookie(session))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode arr = json.readTree(result.getResponse().getContentAsString());
        return arr.get(0).get("id").asLong();
    }

    private Cookie obtainSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/alerts"))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(SessionIdHolder.COOKIE_NAME);
    }
}
