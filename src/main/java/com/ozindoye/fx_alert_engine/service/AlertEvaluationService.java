package com.ozindoye.fx_alert_engine.service;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import com.ozindoye.fx_alert_engine.model.CurrencyPair;
import com.ozindoye.fx_alert_engine.repository.AlertSubscriptionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AlertEvaluationService {

    private final AlertSubscriptionRepository alertSubscriptionRepository;
    private final WebhookDeliveryService webhookDeliveryService;
    private final EmailDeliveryService emailDeliveryService;

    public AlertEvaluationService(AlertSubscriptionRepository alertSubscriptionRepository,
                                  WebhookDeliveryService webhookDeliveryService,
                                  EmailDeliveryService emailDeliveryService) {
        this.alertSubscriptionRepository = alertSubscriptionRepository;
        this.webhookDeliveryService = webhookDeliveryService;
        this.emailDeliveryService = emailDeliveryService;
    }

    public void evaluate(CurrencyPair pair, BigDecimal rate) {
        List<AlertSubscription> subscriptions =
                alertSubscriptionRepository.findByCurrencyPairAndActiveTrue(pair);

        String pairLabel = pair.getBase() + "/" + pair.getQuote();

        for (AlertSubscription subscription : subscriptions) {
            boolean triggered = isTriggered(subscription, rate);
            if (triggered) {
                System.out.println("Alert triggered for " + subscription.getUserEmail()
                        + " — rate: " + rate
                        + " threshold: " + subscription.getThresholdValue());

                emailDeliveryService.sendAlert(subscription, rate, pairLabel);

                if (subscription.getWebhookUrl() != null) {
                    webhookDeliveryService.deliver(subscription, rate, pairLabel);
                }
            }
        }
    }

    private boolean isTriggered(AlertSubscription subscription, BigDecimal rate) {
        return switch (subscription.getThresholdType()) {
            case ABOVE -> rate.compareTo(subscription.getThresholdValue()) > 0;
            case BELOW -> rate.compareTo(subscription.getThresholdValue()) < 0;
        };
    }
}