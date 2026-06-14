package com.ozindoye.fx_alert_engine.repository;

import com.ozindoye.fx_alert_engine.model.AlertSubscription;
import com.ozindoye.fx_alert_engine.model.CurrencyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertSubscriptionRepository extends JpaRepository<AlertSubscription, Long> {

    List<AlertSubscription> findByCurrencyPairAndActiveTrue(CurrencyPair currencyPair);
}
