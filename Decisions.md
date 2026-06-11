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
- We will use `ddl-auto=validate` in Spring Boot — it checks models match the DB but never touches tables

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

*Last updated: Step 2 — Schema created and verified in MySQL Workbench*