# FX Rate Alert Engine

A Spring Boot backend service that polls live foreign exchange rates on a schedule and fires alerts — via email and webhook — when a rate crosses a user-defined threshold.

Built as a backend engineering portfolio project with a focus on the practices that matter in fintech: precise decimal handling, an append-only audit trail, professional schema ownership, retry logic with exponential backoff, and unit-tested business logic.

---

## What It Does

Every 30 seconds the engine:

1. Polls the live USD/GBP rate from the Frankfurter API
2. Saves the rate to an append-only history table
3. Checks the rate against every active alert subscription
4. If a threshold is crossed, sends an email and (optionally) delivers a webhook
5. Logs every delivery attempt — success or failure — to an audit table

Users register alerts through a REST API, specifying the currency pair, a threshold, whether they want to be alerted when the rate goes ABOVE or BELOW it, and an optional webhook URL.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.14 |
| Database | MySQL |
| Persistence | Spring Data JPA |
| Scheduling | Spring `@Scheduled` |
| Retry | Spring Retry (`@Retryable`) |
| Email | SendGrid |
| Build | Maven |
| Testing | JUnit 5 + Mockito |

---

## Architecture

```
Every 30 seconds:
FxRatePoller (@Scheduled)
    -> FxRateClient            fetches rate from Frankfurter API
    -> RateHistoryRepository   saves rate (append-only)
    -> AlertEvaluationService  finds active subscriptions, checks thresholds
        -> EmailDeliveryService    sends alert email via SendGrid
        -> WebhookDeliveryService  POSTs JSON payload, retries with backoff
            -> DeliveryLogRepository  logs SUCCESS or FAILED
```

### Package structure

```
com.ozindoye.fx_alert_engine
├── controller   REST endpoints
├── dto          request/response objects
├── model        JPA entities
├── repository   Spring Data repositories
├── scheduler    polling and API client
└── service      business logic
```

---

## Design Decisions

**BigDecimal everywhere for monetary values.** Floating point cannot represent decimal fractions exactly, which makes `double` and `float` unsafe for financial data. All rates use `BigDecimal` in Java and `DECIMAL(18,6)` in MySQL.

**Append-only rate history.** Rate records are never updated, only inserted. This produces a full audit trail of every rate the engine has ever seen — mirroring how real trading systems handle market data.

**Manual schema ownership.** The database schema is written by hand in SQL and Spring runs with `ddl-auto=validate`, which checks the entities match the tables but never modifies them. This reflects professional practice where the application does not own the schema.

**DTO pattern.** The REST API uses dedicated request and response objects rather than exposing JPA entities directly. This decouples the API contract from the database model and avoids lazy-loading serialisation issues.

**Exponential backoff on webhook delivery.** Failed webhook deliveries are retried up to three times with increasing delays (1s, 2s) using Spring Retry, so a struggling receiver is given time to recover rather than being hammered.

**Delivery audit log.** Every alert delivery attempt is recorded with its outcome and any error message, making the system debuggable and auditable.

---

## Database Schema

Four tables:

- **currency_pairs** — the pairs being watched (e.g. USD/GBP)
- **rate_history** — append-only log of every fetched rate
- **alert_subscriptions** — user-registered alerts with thresholds
- **delivery_log** — audit trail of every delivery attempt

The full schema is in [`db/schema.sql`](db/schema.sql).

---

## REST API

### Create a subscription

```
POST /subscriptions
```

```json
{
  "base": "USD",
  "quote": "GBP",
  "userEmail": "user@example.com",
  "thresholdType": "ABOVE",
  "thresholdValue": 0.75,
  "webhookUrl": "https://example.com/webhook"
}
```

Returns `201 Created` with the saved subscription. Input is validated — a malformed email or missing field returns `400 Bad Request`.

### Get a subscription

```
GET /subscriptions/{id}
```

Returns `200 OK` with the subscription, or `404 Not Found` if it does not exist.

---

## Running Locally

### Prerequisites

- Java 17
- MySQL running locally
- A SendGrid account with a verified sender (for email delivery)

### Setup

1. Create the database and tables:
   ```bash
   mysql -u root -p < db/schema.sql
   ```

2. Create `src/main/resources/application-local.properties` (this file is gitignored and holds your secrets):
   ```properties
   spring.datasource.password=YOUR_DB_PASSWORD
   sendgrid.api.key=YOUR_SENDGRID_API_KEY
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The engine will start polling immediately. Register a subscription via the API and watch the console as alerts fire.

---

## Testing

```bash
./mvnw test
```

The core alert evaluation logic is unit tested with JUnit 5 and Mockito, covering all four ABOVE/BELOW threshold scenarios in isolation from the database and external services.

---

## Configuration & Secrets

Secrets are kept out of version control entirely. `application.properties` is committed and contains only non-secret configuration and placeholders. Real credentials live in `application-local.properties`, which is gitignored. Spring merges the two at startup via the `local` profile.

---

## Roadmap

- [ ] Support multiple currency pairs simultaneously
- [ ] List and delete endpoints for subscriptions
- [ ] Flyway database migrations
- [ ] React frontend dashboard
- [ ] Anomaly detection on rate history