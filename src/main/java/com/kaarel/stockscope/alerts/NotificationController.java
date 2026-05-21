package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.BulkReadResponse;
import com.kaarel.stockscope.alerts.dto.NotificationDto;
import com.kaarel.stockscope.session.SessionId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationDto> list(
            @SessionId String sessionId,
            @RequestParam(name = "unread", defaultValue = "false") boolean unreadOnly) {
        return service.findForSession(sessionId, unreadOnly);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@SessionId String sessionId) {
        return Map.of("count", service.unreadCount(sessionId));
    }

    @PatchMapping("/{id}")
    public NotificationDto markRead(@SessionId String sessionId, @PathVariable Long id) {
        return service.markRead(sessionId, id);
    }

    @PostMapping("/read-all")
    public BulkReadResponse markAllRead(@SessionId String sessionId) {
        return new BulkReadResponse(service.markAllRead(sessionId));
    }
}
