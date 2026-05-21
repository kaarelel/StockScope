package com.kaarel.stockscope.alerts;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllBySessionIdOrderByCreatedAtDesc(String sessionId);

    List<Notification> findAllBySessionIdAndReadFalseOrderByCreatedAtDesc(String sessionId);

    long countBySessionId(String sessionId);

    long countBySessionIdAndReadFalse(String sessionId);

    Optional<Notification> findByIdAndSessionId(Long id, String sessionId);

    @Modifying
    @Query("update Notification n set n.read = true where n.sessionId = :sessionId and n.read = false")
    int markAllReadForSession(@Param("sessionId") String sessionId);

    @Query("select n.id from Notification n where n.sessionId = :sessionId order by n.createdAt asc, n.id asc")
    List<Long> findOldestIdsForSession(@Param("sessionId") String sessionId, Pageable pageable);
}
