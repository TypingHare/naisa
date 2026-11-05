# Iteration 2 Plan

For Iteration 2, we'll keep improving our existing operations and add the new
pieces the assignment requires. Each of us leads one main area, and we'll all
keep testing and documenting our own operations. We'll track individual tasks in
GitHub Issues.

## Who Leads What

| Member  | Our Operations                         | Iteration 2 Focus               |
| ------- | -------------------------------------- | ------------------------------- |
| James   | Architecture and shared code           | Testing infrastructure and CI   |
| Andrew  | Ops 1–4 (search, alternatives, info)   | Independent client              |
| Emily   | Ops 5–7 (compare, crowd report/status) | Google Cloud deployment         |
| Vincent | Ops 8–9 (add venue, reviews)           | Static analysis and E2E testing |

## Main Areas

**Testing infrastructure and CI (James)** In Iteration 1, CI only ran the build,
unit tests, and linting. Now it needs to run every kind of test, static
analysis, and coverage, and save the reports in the repo. James will finish the
shared test setup (PR #86) so we can all write integration and API tests the
same way.

**Independent client (Andrew)** This is new. We need a separate client that only
uses our public API, so it could work with any service that implements the same
endpoints. Multiple copies should be able to run at the same time, and the
README should explain how the service tells them apart (API key per client,
`externalUserId` per user).

**Google Cloud deployment (Emily)** This is new. We'll containerize the service,
deploy it to Cloud Run with a Cloud SQL database, and make sure the mentor can
use it during the demo. The deployment setup and instructions will be in the
repo.

**Static analysis and E2E testing (Vincent)** Both are new. Vincent will run a
bug-finding tool (e.g. SpotBugs), commit a report before we fix the issues and
another one after. Once the client and the cloud service are ready, he'll lead
E2E testing with a checklist we can re-run.
