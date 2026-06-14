package com.ozindoye.fx_alert_engine;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class FxRatePoller {

    private final FxRateClient fxRateClient;

    public FxRatePoller(FxRateClient fxRateClient) {
        this.fxRateClient = fxRateClient;
    }

    @Scheduled(fixedRate = 30000)
    public void pollRates() {
        BigDecimal rate = fxRateClient.fetchRate("USD", "GBP");
        System.out.println("Fetched USD/GBP rate: " + rate);
    }
}