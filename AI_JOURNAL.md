# AI working journal

Tool: Claude Code. Claude drafts the entries from the real conversation. Shafiq checks them. Times are Malaysia time.

## 25 Nov 2026, planning

- **Asked:** Read the brief and say whether it is possible, with about 30 minutes of my own time.
- **AI proposed:** One Spring Boot service, PostgreSQL, REST, Kafka and three Angular screens.
- **Challenged:** I do not know Kafka or Angular.
- **Result:** The AI changed its recommendation to no Kafka and a smaller Angular part. I picked that option (C).
- **Why:** I must be able to explain everything I submit. The backend is the priority in the brief.
- **15:27:** Git repository created and design draft started. No application code yet.
- **15:36:** I approved the design with no rule changes. The AI wrote the build plan and started the backend.

## 25 Nov 2026, backend build

- **AI choice, not in the approved design:** hide the reserve from claimants. The reserve is the insurer's own cost estimate, so claimants get null and do not see reserve changes in the timeline. **My review: accepted.**
- **AI choice, differs from the approved design:** the reference (CLM-000001) is made from the id and not stored. **My review: accepted.**
- **Test order:** the service files were meant to come first, but a tool error stopped them being saved. The API test ran without them and failed 10 of 10, then passed once they were added. This was not planned test-first work.
- **15:47:** 16 tests pass on H2. Not run on PostgreSQL yet.
- **15:48:** backend started on H2 and answered real HTTP requests (login, submit, summary, Swagger).
- **Waiting at this point:** Node.js install approval for Angular, and Docker Desktop for PostgreSQL.
- **15:49:** I approved the Node.js install. Angular 22.3.0 was chosen because it supports Node 26.
- **Challenged:** I do not know PostgreSQL. I use Informix at work. **Result: changed to SQL Server, see below.**

## 25 Nov 2026, frontend build

- **AI built:** login, claimant screen and officer screen. I have not reviewed this code yet.
- **16:02:** the AI ran the full flow in a browser against H2: submit, pick up, ask, answer, set reserve, approve, settle. Wrong login, future date, and hidden reserve also behaved as designed. No automated frontend tests.
- **Open at this point:** database choice and a run on the real database.

## 25 Nov 2026, database change

- **AI proposed:** keep PostgreSQL.
- **Overrode:** I chose SQL Server. I do not know PostgreSQL. I use Informix at work and wanted SQL that I know.
- **AI changed:** driver, connection, table script, test database mode, Docker file. No Java code changed.
- **Problem found:** H2 has no `datetimeoffset` type, so all 10 API tests failed. Fixed by adding the type in the test database only.
- **AI choice:** `nvarchar` columns, so Chinese and Thai text is kept. **My review: accepted.**
- **16:12:** 16 tests pass on H2 and on SQL Server 2025 Express.
- **16:14:** the app ran on SQL Server. Chinese text was saved correctly. Two officers picking up the same claim together, 10 rounds: one 200 and one 409 each time.
- **Not done:** the Docker start was not run, only its syntax was checked.
- **16:19:** I accepted three AI choices: hidden reserve, reference made from the id, and nvarchar columns.

## 26 Nov 2026, check against the brief

- **Asked:** make sure the work follows the "Approach" part of the brief (backend first).
- **Result:** the build order follows it. The README did not explain the priority, so the AI added that section. No code changed.
- **05:49:** recorded.
