# Claims platform build plan

**Goal:** Build the approved design in `docs/design.md`, backend first.

**Tech stack:** Java 17, Spring Boot 3, Maven, JPA, Flyway, PostgreSQL, H2 for tests, Angular.

**Method:** One session, one commit per task, tests with each backend task. Clock started 25 Nov 2026 15:27. Hard stop 20:27.

## Global constraints

- No Kafka, no microservices, no extra layers.
- Package `com.claims`. Layers: controller, service, repository, entity.
- Business rules live in `Claim`. Access rules live on the server.
- Money uses `BigDecimal` and `numeric(14,2)`. Totals are never added across currencies.

## Review focus

1. Page number below 0 or page size above 100 returns 400.
2. Blank reason, future incident date, zero amount and unknown currency return 400.
3. A second officer who tries to decide a claim gets 403.
4. A stale update does not overwrite a newer one and returns 409.
5. A missing claim and another claimant's claim look the same: 404.

## Task 1: Project and claim rules

Files: `backend/pom.xml`, `ClaimsApplication`, `claim/Claim`, `ClaimStatus`, `ClaimType`, `InvalidTransitionException`. Test: `ClaimTest`.

`Claim` methods: `pickUp(officer)`, `requestInfo(officer)`, `answerInfo()`, `changeReserve(officer, amount)`, `approve(officer, amount)`, `reject(officer, reason)`, `settle(officer)`.

- [ ] Tests: full path to SETTLED, reject path, wrong status throws `InvalidTransitionException`, unassigned officer throws `AccessDeniedException`, reserve starts as the estimate.
- [ ] Run `./mvnw test`. Commit.

## Task 2: API, database and security

Files: `V1__claims.sql`, `ClaimActivity`, repositories, `ClaimService`, `ClaimController`, `ClaimChanged`, `NotificationListener`, `SecurityConfig`, `ApiExceptionHandler`, `application.yml`, `compose.yaml`. Test: `ClaimApiTest`.

- [ ] Tests: main flow through HTTP, 401, 403, 404 for another claimant, 400 cases, second pick-up 409, stale update rejected, summary by currency.
- [ ] Run `./mvnw verify`. Commit.

## Task 3: Angular

Files: `frontend/`. Login form, auth interceptor, role guard, claimant screen, officer screen, dev proxy to port 8080.

- [ ] `ng build` passes and both screens work in the browser. Commit.

## Task 4: Handover

- [ ] README with start steps, decisions, limits. Walkthrough notes. Run from a clean start on PostgreSQL. Commit.
