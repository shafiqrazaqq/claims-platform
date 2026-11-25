# Claims platform

Claimants submit and track motor and property claims. Claims officers review, decide and settle them, and see workload and outstanding liability.

## Start it

Needs Java 17, Node.js 26, and SQL Server (local or Docker). Set `JAVA_HOME` to Java 17.

1. Database and backend, pick one:
   - **Local SQL Server on Windows** (port 1433): create a database named `claimsdb`. From `backend`, run `powershell -ExecutionPolicy Bypass -File run-sqlexpress.ps1`. It uses your Windows login.
   - **Docker**: set `ACCEPT_EULA=Y` (Microsoft licence terms) and `DB_PASSWORD` (a strong password). From this folder, run `docker compose up -d`. When `create-db` has finished, run `./mvnw spring-boot:run` from `backend` in the same terminal.
2. Frontend, from `frontend`: `npm ci`, then `npm start`
3. Open http://localhost:4200

API documentation: http://localhost:8080/swagger-ui.html

Demo users: `alice` and `bob` are claimants, `olivia` and `omar` are officers. The shared local password is `app.demo-password` in `backend/src/main/resources/application.yml`.

Run the backend tests from `backend` with `./mvnw verify`. They use H2, so Docker is not needed.

## Structure

```text
Angular -> REST -> Spring Boot (controller -> service -> repository) -> SQL Server
                          |
                          +-> event after commit -> notification log
```

Status rules are in `Claim.java`. Access rules are in `SecurityConfig.java`. The full design is in [docs/design.md](docs/design.md).

## Priorities

The backend came first. The frontend was extended only after the backend tests passed.

- **Backend:** claim rules, access rules, concurrent updates, errors and liability, with 16 automated tests.
- **Frontend:** a login form and two screens for the main flow. It holds no business rules and has no automated tests.
- **Reason:** the server must enforce every rule whichever client calls it.

## Main decisions

- **One service.** One small domain and one transaction per action. Splitting it would add network failures with no benefit yet.
- **REST for user actions.** The user needs the answer at once, including a refusal.
- **SQL Server.** Chosen over PostgreSQL because it is a database I have worked with. Text columns are `nvarchar` for APAC scripts.
- **No Kafka yet.** No other service needs the events. An outbox table and Kafka come when one does.
- **Timeline in the same transaction.** The audit record cannot be lost. A notification can be lost if the service crashes after commit.
- **Optimistic locking.** Two officers cannot overwrite each other. The second gets HTTP 409.
- **Liability per currency.** Amounts in different currencies are not added.
- **Reserve hidden from claimants.** It is the insurer's internal cost estimate.

## Known limits

- HTTP Basic with fixed demo users. Production needs OAuth 2 / OIDC and HTTPS.
- The automated tests use H2. They were also run once on SQL Server 2025 Express. Testcontainers would make that automatic.
- The Docker start was not run, only checked for syntax.
- The local Docker setup uses the `sa` login. Production needs a limited login.
- No frontend tests.
- No file upload, email, payment, reassignment, approval limits, handling time, or live screen updates.
- Any officer sees all markets.

## Next

1. Outbox and Kafka for claim events, with a notification service.
2. OAuth 2 / OIDC, and officer access per market.
3. Testcontainers and frontend tests.
4. Document upload.

AI use is recorded in [AI_JOURNAL.md](AI_JOURNAL.md).
