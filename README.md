# Claims platform

Claimants submit and track motor and property claims. Claims officers review, decide and settle them, and see workload and outstanding liability.

## Start it

Needs Java 17, Docker and Node.js 26. Set `JAVA_HOME` to Java 17.

1. Database, from this folder: `docker compose up -d --wait`
2. Backend, from `backend`: `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`)
3. Frontend, from `frontend`: `npm ci`, then `npm start`
4. Open http://localhost:4200

API documentation: http://localhost:8080/swagger-ui.html

Demo users: `alice` and `bob` are claimants, `olivia` and `omar` are officers. The shared local password is `app.demo-password` in `backend/src/main/resources/application.yml`.

Run the backend tests from `backend` with `./mvnw verify`. They use H2, so Docker is not needed.

## Structure

```text
Angular -> REST -> Spring Boot (controller -> service -> repository) -> PostgreSQL
                          |
                          +-> event after commit -> notification log
```

Status rules are in `Claim.java`. Access rules are in `SecurityConfig.java`. The full design is in [docs/design.md](docs/design.md).

## Main decisions

- **One service.** One small domain and one transaction per action. Splitting it would add network failures with no benefit yet.
- **REST for user actions.** The user needs the answer at once, including a refusal.
- **No Kafka yet.** No other service needs the events. An outbox table and Kafka come when one does.
- **Timeline in the same transaction.** The audit record cannot be lost. A notification can be lost if the service crashes after commit.
- **Optimistic locking.** Two officers cannot overwrite each other. The second gets HTTP 409.
- **Liability per currency.** Amounts in different currencies are not added.
- **Reserve hidden from claimants.** It is the insurer's internal cost estimate.

## Known limits

- HTTP Basic with fixed demo users. Production needs OAuth 2 / OIDC and HTTPS.
- Tests use H2, not PostgreSQL. Testcontainers would close that gap.
- No frontend tests.
- No file upload, email, payment, reassignment, approval limits, handling time, or live screen updates.
- Any officer sees all markets.

## Next

1. Outbox and Kafka for claim events, with a notification service.
2. OAuth 2 / OIDC, and officer access per market.
3. Testcontainers and frontend tests.
4. Document upload.

AI use is recorded in [AI_JOURNAL.md](AI_JOURNAL.md).
