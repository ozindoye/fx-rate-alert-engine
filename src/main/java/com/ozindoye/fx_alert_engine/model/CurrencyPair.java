package com.ozindoye.fx_alert_engine.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "currency_pairs")
public class CurrencyPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base", nullable = false, length = 3)
    private String base;

    @Column(name = "quote", nullable = false, length = 3)
    private String quote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getBase() { return base; }
    public String getQuote() { return quote; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setBase(String base) { this.base = base; }
    public void setQuote(String quote) { this.quote = quote; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}