package com.ozindoye.fx_alert_engine.scheduler;

import com.ozindoye.fx_alert_engine.model.CurrencyPair;
import com.ozindoye.fx_alert_engine.model.RateHistory;
import com.ozindoye.fx_alert_engine.repository.CurrencyPairRepository;
import com.ozindoye.fx_alert_engine.repository.RateHistoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class FxRatePoller {

    private final FxRateClient fxRateClient;
    private final RateHistoryRepository rateHistoryRepository;
    private final CurrencyPairRepository currencyPairRepository;

    public FxRatePoller(FxRateClient fxRateClient,
                        RateHistoryRepository rateHistoryRepository,
                        CurrencyPairRepository currencyPairRepository) {
        this.fxRateClient = fxRateClient;
        this.rateHistoryRepository = rateHistoryRepository;
        this.currencyPairRepository = currencyPairRepository;
    }

    @Scheduled(fixedRate = 30000)
    public void pollRates() {
        BigDecimal rate = fxRateClient.fetchRate("USD", "GBP");

        CurrencyPair pair = currencyPairRepository
                .findByBaseAndQuote("USD", "GBP")
                .orElseGet(() -> {
                    CurrencyPair newPair = new CurrencyPair();
                    newPair.setBase("USD");
                    newPair.setQuote("GBP");
                    return currencyPairRepository.save(newPair);
                });

        RateHistory rateHistory = new RateHistory();
        rateHistory.setCurrencyPair(pair);
        rateHistory.setRate(rate);

        rateHistoryRepository.save(rateHistory);

        System.out.println("Saved USD/GBP rate: " + rate);
    }
}