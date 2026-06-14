# FX Rate Alert Engine — Decision Log

## Environment & Tooling

| Decision | Choice | Why |
|----------|--------|-----|
| **IDE** | IntelliJ | Built for Java/Spring; industry standard in fintech; better Spring support than VS Code |
| **Java version** | Java 17 (Temurin) | LTS version; widely used in production fintech teams |
| **Spring Boot version** | 3.5.14 | Latest stable non-snapshot 3.x release; 4.x too new for a portfolio project |
| **Build tool** | Maven | Standard for enterprise Java; most fintech shops use it |
| **Database** | MySQL (local, MySQL80) | Relational; appropriate for structured financial data |
| **Database GUI** | MySQL Workbench | Visual tool to inspect tables and run queries during development |

---

## Project Setup

| Decision | Choice | Why |
|----------|--------|-----|
| **Packaging** | Jar | Standard for Spring Boot; runs with embedded Tomcat |
| **Group ID** | `com.ozindoye` | Reverse domain convention for Java packages |
| **Artifact** | `fx-alert-engine` | Becomes the folder name and JAR name |

---

## Dependencies

| Dependency | Why |
|------------|-----|
| **Spring Web** | REST endpoints + embedded Tomcat server |
| **Spring Data JPA** | Database access without raw SQL boilerplate |
| **MySQL Driver** | JDBC driver so Java can talk to MySQL |
| **Validation** | `@NotNull`, `@Email` annotations on models |
| **Scheduler** | Built into Spring Boot core — no extra dependency needed |

---

## Database Schema

### Database name: `fx_alert_db`

### Why we wrote SQL manually instead of letting Spring Boot generate tables
- In professional teams, the application does not own the schema
- `ddl-auto=create` drops and recreates tables on restart — data loss risk
- Manual SQL lets us use MySQL-specific features (ENUM, custom indexes)
- Mirrors real practice — schema changes go through migration scripts (Flyway/Liquibase)
- We use `ddl-auto=validate` in Spring Boot — it checks models match the DB but never touches tables

### Table: `currency_pairs`
| Decision | Choice | Why |
|----------|--------|-----|
| `base` / `quote` type | `VARCHAR(3)` | Currency codes are always exactly 3 characters (USD, GBP, EUR) |
| Unique constraint | `UNIQUE KEY uq_base_quote (base, quote)` | Database enforces no duplicate pairs — not just application code |

### Table: `rate_history`
| Decision | Choice | Why |
|----------|--------|-----|
| Write strategy | Append-only (INSERT only, never UPDATE) | Full audit trail; mirrors how real trading systems work; auditability and debugging |
| `rate` type | `DECIMAL(18,6)` | Never use FLOAT or DOUBLE for monetary values — precision loss |
| `pair_id` | Foreign key to `currency_pairs` | Database rejects any rate row that references a non-existent pair |
| `fetched_at` | `updatable = false` | Fetch timestamps should never be changed once written |

### Table: `alert_subscriptions`
| Decision | Choice | Why |
|----------|--------|-----|
| `threshold_type` | `ENUM('ABOVE', 'BELOW')` | Only two valid values; database enforces this — no application bug can insert a typo |
| `webhook_url` | Nullable | Not all users have a system to receive webhooks; some only need email — supports both use cases in one table |
| `active` | `BOOLEAN DEFAULT TRUE` | Allows pausing an alert without deleting it — preserves history |

---

## Numeric Precision Rule
**Always use `BigDecimal` in Java. Always use `DECIMAL(18,6)` in MySQL. Never use `double`, `float`, `FLOAT`, or `DOUBLE` for any monetary or rate value.**

Reason: Floating point cannot represent 0.1 exactly in binary. For exchange rates and money, that is a bug.

---

## Configuration

### Profile strategy
- `application.properties` — committed to GitHub; no secrets
- `application-local.properties` — gitignored; contains real DB password
- `spring.profiles.active=local` merges both files on startup
- `*-local.properties` in `.gitignore` prevents accidental commits
- Mirrors professional practice before graduating to AWS Secrets Manager or Vault

### JPA settings
| Setting | Value | Why |
|---------|-------|-----|
| `ddl-auto` | `validate` | Spring checks models match DB but never touches tables |
| `show-sql` | `true` | Every SQL query printed to console for debugging |
| `format_sql` | `true` | Makes printed SQL readable |

---

## Code Architecture

### Package structure
```
com.ozindoye.fx_alert_engine
├── model        — CurrencyPair, RateHistory
├── repository   — CurrencyPairRepository, RateHistoryRepository
└── scheduler    — FxRatePoller, FxRateClient
```
Chosen over flat structure for clean separation of concerns and professional readability.

### Class responsibilities
| Class | Responsibility |
|-------|---------------|
| `FxRatePoller` | Owns the @Scheduled timer; orchestrates the poll cycle |
| `FxRateClient` | Owns the HTTP call to the Frankfurter API; returns BigDecimal |
| `CurrencyPair` | JPA entity mapping to `currency_pairs` table |
| `RateHistory` | JPA entity mapping to `rate_history` table |
| `CurrencyPairRepository` | DB access for currency pairs; findByBaseAndQuote method |
| `RateHistoryRepository` | DB access for rate history; save method |

### Why constructor injection over `new`
Creating dependencies with `new` inside a class tightly couples them and makes testing hard. Constructor injection lets Spring manage instances. You can swap implementations without touching dependent classes.

### Why `orElseGet` for currency pair lookup
`findByBaseAndQuote` returns an `Optional`. `orElseGet` runs a fallback block only if nothing was found. First poll creates the USD/GBP row. Every subsequent poll reuses it. Prevents duplicate `currency_pairs` rows.

### External API: Frankfurter
- URL: `https://api.frankfurter.app/latest?from={base}&to={quote}`
- Free, no API key required
- Returns JSON parsed into a Java Map; rate extracted and converted to BigDecimal

---

## GitHub
- Repository: https://github.com/ozindoye/fx-rate-alert-engine
- Visibility: Public

### Commit convention
| Prefix | When to use |
|--------|-------------|
| `chore:` | Setup, config, tooling |
| `config:` | Application configuration changes |
| `feat:` | New features |
| `fix:` | Bug fixes |

---

*Last updated: Day 1 complete — first rate fetched and saved to DB, verified in MySQL Workbench*