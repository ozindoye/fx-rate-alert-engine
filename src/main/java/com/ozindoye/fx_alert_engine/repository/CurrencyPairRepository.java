package com.ozindoye.fx_alert_engine.repository;

import com.ozindoye.fx_alert_engine.model.CurrencyPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CurrencyPairRepository extends JpaRepository<CurrencyPair, Long> {

    Optional<CurrencyPair> findByBaseAndQuote(String base, String quote);
}