# FX Rate Alert Engine — My Decision Log

---

## Why I Built This

I wanted a portfolio project that would signal to fintech engineers that I understand how financial systems work. The specific details — using BigDecimal instead of double, writing SQL manually, append-only history, exponential backoff on retries — are the kind of things senior engineers notice and ask about.

---

## Tools & Setup

I used **IntelliJ** because it's the industry standard for Java/Spring work and I already had experience with it. VS Code can do Spring Boot but it's noticeably less smooth.

I went with **Java 17** because it's what I had installed and it's an LTS version still widely used in production fintech teams. Java 21 is growing but 17 is completely fine for this project.

**Spring Boot 3.5.14** — I picked this because it's the latest stable release in the 3.x line. I avoided snapshots (unfinished) and 4.x (too new, too many rough edges for a portfolio project).

**Maven** over Gradle — standard for enterprise Java. Most fintech shops use it.

**MySQL** locally with **MySQL Workbench** to visually inspect tables during development.

---

## Project Scaffolding

- Group ID: `com.ozindoye` — reverse domain convention for Java packages
- Artifact: `fx-alert-engine`
- Packaging: Jar — standard for Spring Boot, runs with embedded Tomcat

Dependencies I added:
- **Spring Web** — for REST endpoints and the embedded Tomcat server
- **Spring Data JPA** — handles database access without writing raw SQL boilerplate
- **MySQL Driver** — JDBC driver so Java can actually talk to MySQL
- **Validation** — for `@NotNull`, `@Email` etc. on request objects
- Scheduling is built into Spring Boot core — no extra dependency needed for `@Scheduled`

Later added:
- **spring-retry** + **spring-aspects** — for `@Retryable` exponential backoff on webhook delivery. spring-aspects is required because @Retryable uses AOP under the hood
- **sendgrid-java 4.10.1** — SendGrid SDK for sending real emails

---

## Database Decisions

### Why I wrote the SQL myself instead of letting Spring generate the tables

I made a deliberate choice to write the schema in SQL and use `ddl-auto=validate` rather than letting Hibernate auto-generate the tables. A few reasons:

- In real teams, the application doesn't own the schema. DBAs and senior engineers control it.
- `ddl-auto=create` drops and recreates tables on every restart — instant data loss
- Writing SQL myself means I can use MySQL-specific features like ENUM and custom indexes
- It mirrors how real schema changes work in production — through migration scripts (Flyway, Liquibase)
- `ddl-auto=validate` means Spring checks my Java models match the DB on startup but never touches the tables themselves

### Database name: `fx_alert_db`

### Table: `currency_pairs`
Stores the pairs I'm watching (e.g. USD/GBP). Everything else points back here.

- `base` and `quote` are `VARCHAR(3)` — currency codes are always exactly 3 characters
- Added a `UNIQUE KEY` on `(base, quote)` so the database itself prevents duplicates, not just my application code

### Table: `rate_history`
This is append-only — I only ever INSERT, never UPDATE. Every poll creates a new row.

- Doing it this way gives a full audit trail of every rate I've ever seen
- `rate` is `DECIMAL(18,6)` — I never use FLOAT or DOUBLE for financial values because floating point can't represent 0.1 exactly in binary. That's a bug waiting to happen.
- `fetched_at` has `updatable = false` — once written, a timestamp should never change

### Table: `alert_subscriptions`
What a user wants to be alerted about.

- `threshold_type` is `ENUM('ABOVE', 'BELOW')` — only two valid values ever exist, so I let the database enforce that
- `webhook_url` is nullable — not every user has a system to receive webhooks. Some just want an email. Making it optional supports both in one table.
- `active` defaults to `TRUE` — lets me pause a subscription without deleting it

### Table: `delivery_log`
Added this as an audit trail for every alert delivery attempt.

- Every time an alert fires, I write a row here with the outcome (SUCCESS or FAILED) and the error message if it failed
- `rate` column records exactly what rate triggered the alert — important for debugging
- `error_message` is nullable — only populated on failure
- This mirrors how real financial systems think about auditability

---

## Numeric Precision Rule

**BigDecimal in Java. DECIMAL(18,6) in MySQL. Never double, float, FLOAT, or DOUBLE for any rate or monetary value.**

Floating point can't represent 0.1 exactly in binary. In a financial context that's not an edge case — it's a guaranteed bug.

---

## Configuration: Secrets Management

I split my config into two files:

- `application.properties` — committed to GitHub, contains no secrets, just structure and placeholders
- `application-local.properties` — gitignored, contains my real DB password and SendGrid API key
- `spring.profiles.active=local` tells Spring to merge both files on startup
- Added `*-local.properties` to `.gitignore` so it can never accidentally be committed

This mirrors how real teams handle secrets before graduating to something like AWS Secrets Manager or HashiCorp Vault.

---

## Code Architecture Decisions

### Package structure

I organised into sub-packages rather than dumping everything in one place:

```
com.ozindoye.fx_alert_engine
├── controller   — HTTP layer
├── dto          — request/response objects
├── model        — JPA entities
├── repository   — database access
├── scheduler    — polling and API client
└── service      — business logic
```

### Two classes for polling: FxRatePoller + FxRateClient

I split the polling logic into two classes rather than one. `FxRatePoller` owns the schedule and orchestrates the cycle. `FxRateClient` owns the HTTP call to the Frankfurter API. This is separation of concerns — each class has one job.

### Why I used @Retryable instead of writing my own retry loop

I could have written a while loop with Thread.sleep for the exponential backoff, but Spring Retry's `@Retryable` is cleaner, better tested, and shows I know the ecosystem. Configuration:
- `maxAttempts = 3`
- `@Backoff(delay = 1000, multiplier = 2)` — waits 1 second, then 2 seconds between retries
- `@Recover` method handles the failure case after all retries are exhausted

### DTO pattern for REST endpoints

I used separate DTO classes instead of exposing my JPA entities directly over the API:

- `CreateSubscriptionRequest` — what the caller sends in. Has validation annotations (`@Email`, `@NotBlank`, `@NotNull`, `@DecimalMin`)
- `SubscriptionResponse` — what I send back. Flat structure with plain fields, no Hibernate proxies

Why: decouples my API contract from my database model. If I rename a column, my API doesn't break. Also prevents the Hibernate lazy loading proxy serialisation error.

### FetchType.LAZY and the proxy problem

I use `FetchType.LAZY` on all `@ManyToOne` relationships — it's more efficient because Hibernate doesn't load related objects unless you actually need them.

The catch: once the JPA session closes, you can't access a LAZY proxy anymore. I hit this twice:

1. `WebhookDeliveryService` trying to call `subscription.getCurrencyPair().getBase()` after the session closed
2. The GET endpoint trying to serialise a LAZY entity directly

Fix in both cases: access the values you need while the session is still open (in the service layer), copy them into plain Strings, and pass those forward. Never pass JPA entities across session boundaries.

### BigDecimal.compareTo() for rate comparison

BigDecimal can't use `>` or `<` operators like primitives can. I use `compareTo()`:
- Returns positive if left > right → ABOVE condition
- Returns negative if left < right → BELOW condition


### EnumType.STRING for all enums

I always use `@Enumerated(EnumType.STRING)` rather than the default `ORDINAL`. ORDINAL stores 0, 1, 2... which means if I ever reorder the enum values, all existing data silently becomes wrong. STRING stores "ABOVE", "BELOW" — safe regardless of order changes.

---

## Unit Tests

I used JUnit 5 + Mockito to test `AlertEvaluationService` — the most critical class because it contains the decision of whether an alert fires.

Pattern I follow: **AAA — Arrange, Act, Assert**

I mocked the dependencies (repository, webhook service, email service) so the tests run in complete isolation — no real database, no real HTTP calls.

Four tests covering the core logic:

| Test | Scenario | Expected |
|------|----------|----------|
| `shouldFireAlert_whenRateIsAboveThreshold` | Rate 0.80, threshold 0.50, ABOVE | Alert fires |
| `shouldNotFireAlert_whenRateIsBelowThreshold_andTypeIsAbove` | Rate 0.40, threshold 0.50, ABOVE | Alert does not fire |
| `shouldFireAlert_whenRateIsBelowThreshold` | Rate 0.40, threshold 0.50, BELOW | Alert fires |
| `shouldNotFireAlert_whenRateIsAboveThreshold_andTypeIsBelow` | Rate 0.80, threshold 0.50, BELOW | Alert does not fire |

All four passing green.

---

## GitHub

Repository: https://github.com/ozindoye/fx-rate-alert-engine

Commit convention I use:
- `chore:` — setup, config, tooling
- `config:` — application configuration
- `feat:` — new features
- `fix:` — bug fixes
- `test:` — unit tests

---

## What's Next

### Phase 2 — Learn React on a separate project
Not bolting a frontend onto this. I'll build a separate full stack project specifically to learn React. This project is a backend API and that's where its value is.

### Phase 3 — Come back and add meaningful AI
Once I understand React and have some AI basics, I'll return to add something genuinely interesting — anomaly detection on rate history, NLP-based subscription creation, or rate trend analysis. Not a shallow ChatGPT wrapper.

---

*Phase 1 complete.*