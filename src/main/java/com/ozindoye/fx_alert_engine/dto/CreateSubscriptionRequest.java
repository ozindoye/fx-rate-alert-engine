package com.ozindoye.fx_alert_engine.dto;

import com.ozindoye.fx_alert_engine.model.AlertSubscription.ThresholdType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class CreateSubscriptionRequest {

    @NotBlank(message = "Base currency is required")
    private String base;

    @NotBlank(message = "Quote currency is required")
    private String quote;

    @Email(message = "Must be a valid email address")
    @NotBlank(message = "Email is required")
    private String userEmail;

    @NotNull(message = "Threshold type is required")
    private ThresholdType thresholdType;

    @NotNull(message = "Threshold value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Threshold must be greater than zero")
    private BigDecimal thresholdValue;

    private String webhookUrl;

    public String getBase() { return base; }
    public String getQuote() { return quote; }
    public String getUserEmail() { return userEmail; }
    public ThresholdType getThresholdType() { return thresholdType; }
    public BigDecimal getThresholdValue() { return thresholdValue; }
    public String getWebhookUrl() { return webhookUrl; }

    public void setBase(String base) { this.base = base; }
    public void setQuote(String quote) { this.quote = quote; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setThresholdType(ThresholdType thresholdType) { this.thresholdType = thresholdType; }
    public void setThresholdValue(BigDecimal thresholdValue) { this.thresholdValue = thresholdValue; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
}
