package com.ozindoye.fx_alert_engine.repository;

import com.ozindoye.fx_alert_engine.model.RateHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RateHistoryRepository extends JpaRepository<RateHistory, Long> {
}