package com.ozindoye.fx_alert_engine;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Component
public class FxRateClient {

    private static final String API_URL =
            "https://api.frankfurter.app/latest?from={base}&to={quote}";

    private final RestTemplate restTemplate = new RestTemplate();

    public BigDecimal fetchRate(String base, String quote) {
        Map<String, Object> response = restTemplate.getForObject(
                API_URL,
                Map.class,
                base,
                quote
        );

        Map<String, Object> rates = (Map<String, Object>) response.get("rates");
        Object rateValue = rates.get(quote);

        if (rateValue instanceof Double) {
            return BigDecimal.valueOf((Double) rateValue);
        }
        return new BigDecimal(rateValue.toString());
    }
}