package com.kaarel.stockscope.alerts;

import com.kaarel.stockscope.alerts.dto.NotificationDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persistence-facing service for notifications.
 * <p>
 * Cap enforcement assumes a single writer per session — in this app the only writer is
 * {@link AlertEvaluator} driven by the singleton {@link AlertScheduler}. If multiple writers
 * are introduced later, {@link #enforceCap(String)} must be protected by a pessimistic lock
 * or moved to a single-instance leader/queue.
 */
@Service
@Transactional
public class NotificationService {

    static final int MAX_NOTIFICATIONS_PER_SESSION = 100;

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> findForSession(String sessionId, boolean unreadOnly) {
        List<Notification> entities = unreadOnly
                ? repository.findAllBySessionIdAndReadFalseOrderByCreatedAtDesc(sessionId)
                : repository.findAllBySessionIdOrderByCreatedAtDesc(sessionId);
        return entities.stream().map(NotificationService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String sessionId) {
        return repository.countBySessionIdAndReadFalse(sessionId);
    }

    public NotificationDto markRead(String sessionId, Long id) {
        Notification notification = repository.findByIdAndSessionId(id, sessionId)
                .orElseThrow(() -> new NotificationNotFoundException(id));
        notification.markRead();
        return toDto(notification);
    }

    public int markAllRead(String sessionId) {
        return repository.markAllReadForSession(sessionId);
    }

    public Notification recordTrigger(String sessionId, Alert alert, double triggeredPrice, String message) {
        Notification saved = repository.save(new Notification(sessionId, alert, triggeredPrice, message));
        enforceCap(sessionId);
        return saved;
    }

    private void enforceCap(String sessionId) {
        long count = repository.countBySessionId(sessionId);
        if (count <= MAX_NOTIFICATIONS_PER_SESSION) {
            return;
        }
        int toDelete = (int) Math.min(count - MAX_NOTIFICATIONS_PER_SESSION, Integer.MAX_VALUE);
        List<Long> oldest = repository.findOldestIdsForSession(sessionId, PageRequest.of(0, toDelete));
        if (!oldest.isEmpty()) {
            repository.deleteAllByIdInBatch(oldest);
        }
    }

    static NotificationDto toDto(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getAlert().getId(),
                n.getSymbol(),
                n.getCondition(),
                n.getTargetPrice(),
                n.getTriggeredPrice(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt());
    }
}
