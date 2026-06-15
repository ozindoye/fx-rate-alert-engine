package com.ozindoye.fx_alert_engine.service;

import com.ozindoye.fx_alert_engine.dto.CreateSubscriptionRequest;
import com.ozindoye.fx_alert_engine.dto.SubscriptionResponse;
import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import com.ozindoye.fx_alert_engine.model.CurrencyPair;
import com.ozindoye.fx_alert_engine.repository.AlertSubscriptionRepository;
import com.ozindoye.fx_alert_engine.repository.CurrencyPairRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AlertSubscriptionService {

    private final AlertSubscriptionRepository alertSubscriptionRepository;
    private final CurrencyPairRepository currencyPairRepository;

    public AlertSubscriptionService(AlertSubscriptionRepository alertSubscriptionRepository,
                                    CurrencyPairRepository currencyPairRepository) {
        this.alertSubscriptionRepository = alertSubscriptionRepository;
        this.currencyPairRepository = currencyPairRepository;
    }

    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        CurrencyPair pair = currencyPairRepository
                .findByBaseAndQuote(request.getBase(), request.getQuote())
                .orElseGet(() -> {
                    CurrencyPair newPair = new CurrencyPair();
                    newPair.setBase(request.getBase());
                    newPair.setQuote(request.getQuote());
                    return currencyPairRepository.save(newPair);
                });

        AlertSubscription subscription = new AlertSubscription();
        subscription.setCurrencyPair(pair);
        subscription.setUserEmail(request.getUserEmail());
        subscription.setThresholdType(request.getThresholdType());
        subscription.setThresholdValue(request.getThresholdValue());
        subscription.setWebhookUrl(request.getWebhookUrl());

        AlertSubscription saved = alertSubscriptionRepository.save(subscription);
        return SubscriptionResponse.from(saved);
    }

    public Optional<SubscriptionResponse> findById(Long id) {
        return alertSubscriptionRepository.findById(id)
                .map(SubscriptionResponse::from);
    }
}