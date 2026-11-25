# Claims platform design

Status: approved by Shafiq on 25 Nov 2026.

## Why

Claimants cannot see what is happening to their claim. Claims staff work from inboxes and spreadsheets. Managers cannot see outstanding claims or liability. This platform gives each of them one shared view.

## Goals

1. A claimant can submit a claim, track it, and answer a request for more information.
2. An officer can pick up a claim, ask for information, approve, reject and settle it.
3. Staff can see claims by status, claims per officer, and outstanding liability.
4. The server enforces the claim rules and access rules. Tests prove them.

## Left out

Kafka, microservices, file upload, real email, payments, user registration, single sign-on, average handling time, a separate manager role. The README will list these with reasons.

This would be a failure even if it runs: code that Shafiq cannot explain to the panel.

## Approaches considered

| Approach | Trade-off |
|---|---|
| A. One service, REST, Kafka, three Angular screens | Covers everything named in the brief. Two technologies Shafiq does not know. |
| B. Backend only | Least to explain. Ignores the Angular requirement. |
| C. One service, REST, events inside the service, two Angular screens | Strong backend. Must justify leaving Kafka out. |

Shafiq picked C because he does not know Kafka or Angular and has limited time. Microservices were ruled out: one small domain, one team, one database transaction per action.

## Architecture

```text
Angular (login, claimant screen, officer screen)
   | REST + JSON
Spring Boot service: controller -> service -> repository
   |                       |
PostgreSQL            in-process event after commit -> notification log
```

- Java 17, Spring Boot 3, Maven, JPA, Spring Security, Bean Validation, Flyway, Swagger/OpenAPI.
- PostgreSQL through Docker Compose. Tests use H2 in PostgreSQL mode.
- The backend owns the API contract and publishes it through OpenAPI.

## Data model

- **Claim**: id, reference (made from the id, not stored), claimant username, market, type (MOTOR or PROPERTY), policy number, incident date, description, currency, estimated amount, reserve amount, approved amount, status, assigned officer, decision reason, version, created and updated times.
- **ClaimActivity**: id, claim id, type, actor, message, time. One claim has many activities. This is the timeline and the audit record.

## Status rules

```text
SUBMITTED      -> UNDER_REVIEW      officer picks up
UNDER_REVIEW   -> INFO_REQUESTED    assigned officer asks a question
INFO_REQUESTED -> UNDER_REVIEW      claimant answers
UNDER_REVIEW   -> APPROVED          assigned officer sets the approved amount
UNDER_REVIEW   -> REJECTED          assigned officer gives a reason
APPROVED       -> SETTLED           assigned officer marks it settled
```

Any other change returns HTTP 409.

## Outstanding liability

| Status | Amount counted |
|---|---|
| SUBMITTED, UNDER_REVIEW, INFO_REQUESTED | Reserve amount |
| APPROVED | Approved amount |
| REJECTED, SETTLED | 0 |

The reserve starts as the claimant's estimate. The assigned officer can change it while the claim is UNDER_REVIEW. Totals are grouped by currency and are never added across currencies. The reserve is internal: claimants never see the amount or its changes.

## API

| Method and path | Who | Purpose |
|---|---|---|
| GET /api/me | Both | Username and role |
| POST /api/claims | Claimant | Submit |
| GET /api/claims | Both | Claimant: own claims. Officer: all, filter by status, paged |
| GET /api/claims/{id} | Both | Detail and timeline |
| POST /api/claims/{id}/info-response | Claimant | Answer a question |
| POST /api/claims/{id}/pick-up | Officer | Assign to self |
| POST /api/claims/{id}/info-request | Assigned officer | Ask a question |
| PUT /api/claims/{id}/reserve | Assigned officer | Change the reserve |
| POST /api/claims/{id}/approve | Assigned officer | Approve |
| POST /api/claims/{id}/reject | Assigned officer | Reject |
| POST /api/claims/{id}/settle | Assigned officer | Settle |
| GET /api/summary | Officer | Counts by status and officer, liability by currency |

Errors use one JSON format. A claimant who requests another person's claim gets 404, so the API does not reveal that it exists.

## Events

The timeline row is saved in the same transaction as the status change, so audit history cannot be lost. A notification event runs after commit and only writes a log line. If the service crashes between commit and notification, that notification is lost. An outbox table and Kafka would fix this, and would be added when another service needs claim events.

## Security

HTTP Basic with demo users: two claimants and two officers, so ownership rules can be tested. Passwords are BCrypt hashed and for local use only. Roles are checked on the server. Angular keeps the login in memory only. Production would use OAuth 2 / OIDC over HTTPS.

## Concurrency

The version column gives optimistic locking. If two officers pick up the same claim together, one succeeds and the other gets 409.

## Frontend

One Angular app with a login form and two role-guarded routes. The claimant screen submits, lists and tracks claims and answers questions. The officer screen shows the summary, the claim list and the action buttons. No business rules live in Angular. The screen refreshes after each action and through a Refresh button.

## Tests

Unit tests for status rules and liability. Integration tests for the main flow, role checks, ownership, validation, and the 409 for two officers picking up the same claim.

## Assumptions

- The brief does not name the six markets, so market is a two-letter country code and currency is a three-letter code.
- One officer owns a claim. There is no reassignment.
- The approved amount may differ from the estimate. Approval limits are left out.

## Left to the builder

Package names, screen styling, exact library versions, sample data.
