package com.ozindoye.fx_alert_engine.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rate_history")
public class RateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id", nullable = false)
    private CurrencyPair currencyPair;

    @Column(name = "rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal rate;

    @Column(name = "fetched_at", nullable = false, updatable = false)
    private LocalDateTime fetchedAt;

    @PrePersist
    protected void onCreate() {
        this.fetchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public CurrencyPair getCurrencyPair() { return currencyPair; }
    public BigDecimal getRate() { return rate; }
    public LocalDateTime getFetchedAt() { return fetchedAt; }

    public void setId(Long id) { this.id = id; }
    public void setCurrencyPair(CurrencyPair currencyPair) { this.currencyPair = currencyPair; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setFetchedAt(LocalDateTime fetchedAt) { this.fetchedAt = fetchedAt; }
}