package com.ozindoye.fx_alert_engine.dto;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import java.math.BigDecimal;

public class SubscriptionResponse {

    private Long id;
    private String base;
    private String quote;
    private String userEmail;
    private String thresholdType;
    private BigDecimal thresholdValue;
    private String webhookUrl;
    private boolean active;

    public static SubscriptionResponse from(AlertSubscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.id = subscription.getId();
        response.base = subscription.getCurrencyPair().getBase();
        response.quote = subscription.getCurrencyPair().getQuote();
        response.userEmail = subscription.getUserEmail();
        response.thresholdType = subscription.getThresholdType().name();
        response.thresholdValue = subscription.getThresholdValue();
        response.webhookUrl = subscription.getWebhookUrl();
        response.active = subscription.isActive();
        return response;
    }

    public Long getId() { return id; }
    public String getBase() { return base; }
    public String getQuote() { return quote; }
    public String getUserEmail() { return userEmail; }
    public String getThresholdType() { return thresholdType; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public String getWebhookUrl() { return webhookUrl; }
    public boolean isActive() { return active; }
}
