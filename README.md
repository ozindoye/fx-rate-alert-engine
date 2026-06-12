# FX Rate Alert Engine

A Spring Boot backend service that polls live foreign exchange rates 
and fires alerts when a rate crosses a user-defined threshold.

## Tech Stack
- Java 17
- Spring Boot 3.5.14
- MySQL
- Spring Data JPA
- Spring Scheduler

## Features (in progress)
- [ ] Live FX rate polling via external API
- [ ] Append-only rate history (full audit trail)
- [ ] Email + webhook alerts on threshold breach
- [ ] BigDecimal precision throughout — no float/double

## Schema
Three tables: `currency_pairs`, `rate_history`, `alert_subscriptions`
