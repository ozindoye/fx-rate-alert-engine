package com.ozindoye.fx_alert_engine.controller;

import com.ozindoye.fx_alert_engine.dto.CreateSubscriptionRequest;
import com.ozindoye.fx_alert_engine.dto.SubscriptionResponse;
import com.ozindoye.fx_alert_engine.service.AlertSubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
public class AlertSubscriptionController {

    private final AlertSubscriptionService alertSubscriptionService;

    public AlertSubscriptionController(AlertSubscriptionService alertSubscriptionService) {
        this.alertSubscriptionService = alertSubscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(
            @RequestBody @Valid CreateSubscriptionRequest request) {
        SubscriptionResponse response = alertSubscriptionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable Long id) {
        return alertSubscriptionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}