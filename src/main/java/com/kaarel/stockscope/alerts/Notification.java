package com.kaarel.stockscope.alerts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false, length = 10)
    private AlertCondition condition;

    @Column(name = "target_price", nullable = false)
    private double targetPrice;

    @Column(name = "triggered_price", nullable = false)
    private double triggeredPrice;

    @Column(name = "message", nullable = false, length = 255)
    private String message;

    @Column(name = "read_flag", nullable = false)
    private boolean read;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(String sessionId, Alert alert, double triggeredPrice, String message) {
        this.sessionId = sessionId;
        this.alert = alert;
        this.symbol = alert.getSymbol();
        this.condition = alert.getCondition();
        this.targetPrice = alert.getTargetPrice();
        this.triggeredPrice = triggeredPrice;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Alert getAlert() {
        return alert;
    }

    public String getSymbol() {
        return symbol;
    }

    public AlertCondition getCondition() {
        return condition;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public double getTriggeredPrice() {
        return triggeredPrice;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        this.read = true;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
