package com.ozindoye.fx_alert_engine.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "alert_subscriptions")
public class AlertSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id", nullable = false)
    private CurrencyPair currencyPair;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "threshold_type", nullable = false)
    private ThresholdType thresholdType;

    @Column(name = "threshold_value", nullable = false, precision = 18, scale = 6)
    private BigDecimal thresholdValue;

    @Column(name = "webhook_url")
    private String webhookUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public enum ThresholdType {
        ABOVE, BELOW
    }

    public Long getId() { return id; }
    public CurrencyPair getCurrencyPair() { return currencyPair; }
    public String getUserEmail() { return userEmail; }
    public ThresholdType getThresholdType() { return thresholdType; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public String getWebhookUrl() { return webhookUrl; }
    public boolean isActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setCurrencyPair(CurrencyPair currencyPair) { this.currencyPair = currencyPair; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setThresholdType(ThresholdType thresholdType) { this.thresholdType = thresholdType; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    public void setActive(boolean active) { this.active = active; }
}