package com.ozindoye.fx_alert_engine.service;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import com.ozindoye.fx_alert_engine.model.DeliveryLog;
import com.ozindoye.fx_alert_engine.model.DeliveryLog.DeliveryStatus;
import com.ozindoye.fx_alert_engine.repository.DeliveryLogRepository;
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
    private final DeliveryLogRepository deliveryLogRepository;

    public WebhookDeliveryService(DeliveryLogRepository deliveryLogRepository) {
        this.deliveryLogRepository = deliveryLogRepository;
    }

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

        log(subscription, rate, DeliveryStatus.SUCCESS, null);
        System.out.println("Webhook delivered to: " + subscription.getWebhookUrl());
    }

    @Recover
    public void recover(Exception e, AlertSubscription subscription,
                        BigDecimal rate, String pairLabel) {
        log(subscription, rate, DeliveryStatus.FAILED, e.getMessage());
        System.out.println("Webhook delivery failed after all retries for: "
                + subscription.getWebhookUrl()
                + " — error: " + e.getMessage());
    }

    private void log(AlertSubscription subscription, BigDecimal rate,
                     DeliveryStatus status, String errorMessage) {
        DeliveryLog entry = new DeliveryLog();
        entry.setSubscription(subscription);
        entry.setRate(rate);
        entry.setStatus(status);
        entry.setErrorMessage(errorMessage);
        deliveryLogRepository.save(entry);
    }
}
