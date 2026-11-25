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

- **AI choice, not in the approved design:** hide the reserve from claimants. The reserve is the insurer's own cost estimate, so claimants get null and do not see reserve changes in the timeline. **My review: pending.**
- **AI choice, differs from the approved design:** the reference (CLM-000001) is made from the id and not stored. **My review: pending.**
- **Order:** the API test was written before the service finished. It failed first (10 of 10), then passed.
- **15:47:** 16 tests pass on H2. Not run on PostgreSQL yet.
