package com.kaarel.stockscope.alerts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findAllBySessionIdOrderByCreatedAtDesc(String sessionId);

    Optional<Alert> findByIdAndSessionId(Long id, String sessionId);

    List<Alert> findAllByActiveTrueOrderByCreatedAtAsc();
}
