package com.ozindoye.fx_alert_engine.repository;

import com.ozindoye.fx_alert_engine.model.DeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryLogRepository extends JpaRepository<DeliveryLog, Long> {

    List<DeliveryLog> findBySubscriptionId(Long subscriptionId);
}
