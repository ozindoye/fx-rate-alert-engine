package com.ozindoye.fx_alert_engine.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_log")
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private AlertSubscription subscription;

    @Column(name = "rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private DeliveryStatus status;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "delivered_at", nullable = false, updatable = false)
    private LocalDateTime deliveredAt;

    @PrePersist
    protected void onCreate() {
        this.deliveredAt = LocalDateTime.now();
    }

    public enum DeliveryStatus {
        SUCCESS, FAILED
    }

    public Long getId() { return id; }
    public AlertSubscription getSubscription() { return subscription; }
    public BigDecimal getRate() { return rate; }
    public DeliveryStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }

    public void setId(Long id) { this.id = id; }
    public void setSubscription(AlertSubscription subscription) { this.subscription = subscription; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
}