package com.ozindoye.fx_alert_engine.service;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class WebhookDeliveryService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public void deliver(AlertSubscription subscription, BigDecimal rate, String pairLabel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pair", pairLabel);
        payload.put("rate", rate);
        payload.put("threshold", subscription.getThresholdValue());
        payload.put("threshold_type", subscription.getThresholdType());
        payload.put("email", subscription.getUserEmail());
        payload.put("triggered_at", Instant.now().toString());

        restTemplate.postForObject(
                subscription.getWebhookUrl(),
                payload,
                String.class
        );

        System.out.println("Webhook delivered to: " + subscription.getWebhookUrl());
    }

    @Recover
    public void recover(Exception e, AlertSubscription subscription, BigDecimal rate, String pairLabel) {
        System.out.println("Webhook delivery failed after all retries for: "
                + subscription.getWebhookUrl()
                + " — error: " + e.getMessage());
    }
}