package com.ozindoye.fx_alert_engine;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import com.ozindoye.fx_alert_engine.model.AlertSubscription.ThresholdType;
import com.ozindoye.fx_alert_engine.model.CurrencyPair;
import com.ozindoye.fx_alert_engine.repository.AlertSubscriptionRepository;
import com.ozindoye.fx_alert_engine.service.AlertEvaluationService;
import com.ozindoye.fx_alert_engine.service.EmailDeliveryService;
import com.ozindoye.fx_alert_engine.service.WebhookDeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AlertEvaluationServiceTest {

    @Mock
    private AlertSubscriptionRepository alertSubscriptionRepository;

    @Mock
    private WebhookDeliveryService webhookDeliveryService;

    @Mock
    private EmailDeliveryService emailDeliveryService;

    private AlertEvaluationService alertEvaluationService;

    private CurrencyPair pair;

    @BeforeEach
    void setUp() {
        alertEvaluationService = new AlertEvaluationService(
                alertSubscriptionRepository,
                webhookDeliveryService,
                emailDeliveryService
        );

        pair = new CurrencyPair();
        pair.setBase("USD");
        pair.setQuote("GBP");
    }

    @Test
    void shouldFireAlert_whenRateIsAboveThreshold() {
        // Arrange
        AlertSubscription subscription = buildSubscription(
                ThresholdType.ABOVE,
                new BigDecimal("0.50"),
                null
        );
        when(alertSubscriptionRepository
                .findByCurrencyPairAndActiveTrue(pair))
                .thenReturn(List.of(subscription));

        // Act
        alertEvaluationService.evaluate(pair, new BigDecimal("0.80"));

        // Assert
        verify(emailDeliveryService, times(1))
                .sendAlert(eq(subscription), eq(new BigDecimal("0.80")), eq("USD/GBP"));
    }

    @Test
    void shouldNotFireAlert_whenRateIsBelowThreshold_andTypeIsAbove() {
        // Arrange
        AlertSubscription subscription = buildSubscription(
                ThresholdType.ABOVE,
                new BigDecimal("0.50"),
                null
        );
        when(alertSubscriptionRepository
                .findByCurrencyPairAndActiveTrue(pair))
                .thenReturn(List.of(subscription));

        // Act
        alertEvaluationService.evaluate(pair, new BigDecimal("0.40"));

        // Assert
        verify(emailDeliveryService, never())
                .sendAlert(any(), any(), any());
    }

    @Test
    void shouldFireAlert_whenRateIsBelowThreshold() {
        // Arrange
        AlertSubscription subscription = buildSubscription(
                ThresholdType.BELOW,
                new BigDecimal("0.50"),
                null
        );
        when(alertSubscriptionRepository
                .findByCurrencyPairAndActiveTrue(pair))
                .thenReturn(List.of(subscription));

        // Act
        alertEvaluationService.evaluate(pair, new BigDecimal("0.40"));

        // Assert
        verify(emailDeliveryService, times(1))
                .sendAlert(eq(subscription), eq(new BigDecimal("0.40")), eq("USD/GBP"));
    }

    @Test
    void shouldNotFireAlert_whenRateIsAboveThreshold_andTypeIsBelow() {
        // Arrange
        AlertSubscription subscription = buildSubscription(
                ThresholdType.BELOW,
                new BigDecimal("0.50"),
                null
        );
        when(alertSubscriptionRepository
                .findByCurrencyPairAndActiveTrue(pair))
                .thenReturn(List.of(subscription));

        // Act
        alertEvaluationService.evaluate(pair, new BigDecimal("0.80"));

        // Assert
        verify(emailDeliveryService, never())
                .sendAlert(any(), any(), any());
    }

    private AlertSubscription buildSubscription(ThresholdType type,
                                                BigDecimal threshold,
                                                String webhookUrl) {
        AlertSubscription subscription = new AlertSubscription();
        subscription.setThresholdType(type);
        subscription.setThresholdValue(threshold);
        subscription.setWebhookUrl(webhookUrl);
        subscription.setUserEmail("test@example.com");
        return subscription;
    }
}
