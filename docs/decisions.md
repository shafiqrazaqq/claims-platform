# Decisions and reasons

I used AI to build this. This page says who made each decision, why, and what it costs.

## I chose these

| Decision | Why | Cost |
|---|---|---|
| Backend first, small frontend (option C) | The backend is the priority, and I am new to Angular. | The frontend is basic. |
| No Kafka | I do not know Kafka, and no other service needs the events yet. I must be able to explain what I submit. | A crash after commit can lose a notification. |
| SQL Server, not PostgreSQL | I do not know PostgreSQL. I use Informix at work and wanted SQL that I know. | The automated tests use H2, so small differences can hide. |

## AI proposed these, and I approved them in the design

| Decision | Why | Cost |
|---|---|---|
| One service | One small domain and one transaction per action. | Parts cannot scale or deploy separately. |
| REST for user actions | The user needs the answer or the refusal at once. | The caller waits. |
| Event after commit for notifications | A notification must not delay or undo a claim action. | It only writes a log line, and a crash can lose it. |
| Claim and activity tables | One claim has many activities. They give the timeline and the audit record. | One more row for each action. |
| Timeline saved in the same transaction | The audit record cannot be lost. | If the timeline row fails, the action fails too. |
| Version column (optimistic locking) | If two officers save the same claim together, the second gets 409. | The second officer must refresh and try again. |
| Reserve starts as the estimate | Liability is known from the first day. | It is only as good as the estimate until an officer changes it. |
| Liability per currency | Amounts in different currencies cannot be added without exchange rates. | No single regional total. |
| 404 for another claimant's claim | 403 would confirm that the claim exists. | A wrong id and a forbidden id look the same. |
| HTTP Basic with demo users | Enough to test roles and ownership locally. | Not for production. Use OAuth 2 / OIDC and HTTPS. |
| Backend defines the API contract (OpenAPI) | The server enforces the rules, so it owns the contract. | The Angular types are written by hand. |
| Flyway scripts | The table changes are versioned with the code. | A change needs a new script. |
| One Angular app, two guarded routes | Both users share the login and the API code. | Both roles download the same app. |
| Login kept in memory in Angular | It is not stored in the browser. | A page reload logs the user out. |
| Refresh button, no live updates | The smallest way to see another user's change. | The user must click. |

## AI chose these while building, and I accepted them

| Decision | Why | Cost |
|---|---|---|
| Reserve hidden from claimants | It is the insurer's own cost estimate. | One response shape has an empty field for claimants. |
| Reference made from the id | No second number to keep unique. | It shows how many claims exist. |
| `nvarchar` text columns | They keep Chinese, Thai and other scripts. | More storage than `varchar`. |

## AI chose these while building, and I have not finished reviewing them

| Decision | Why | Cost |
|---|---|---|
| Rules inside `Claim`, no setters | Every action passes the same checks. | The entity holds logic as well as data. |
| Input checks on request records | Bad input stops before the service. | The form repeats some checks for the user. |
| One JSON error format | The frontend reads every error in the same way. | One shared error class. |
| CSRF protection off, no sessions | Angular sends the login header itself. No session cookie is used. | Must be reviewed if cookies are added. |
| H2 for automated tests | Tests run without a database install. | It is not the real database. |
| No automated frontend tests | Time went to the backend tests. | Screen errors are found only by hand. |

## Left out

See "Known limits" and "Next" in the [README](../README.md).
