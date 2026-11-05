# Iteration 2 Plan

For Iteration 2, we'll keep improving our existing operations and add the new pieces the assignment requires. Each of us leads one main area, and we'll all keep testing and documenting our own operations. We'll track individual tasks in GitHub Issues.

## Who Leads What

| Member | Our Operations | Iteration 2 Focus |
|---|---|---|
| James | Architecture and shared code | Testing infrastructure and CI |
| Andrew | Ops 1–4 (search, alternatives, info) | Independent client |
| Emily | Ops 5–7 (compare, crowd report/status) | Google Cloud deployment |
| Vincent | Ops 8–9 (add venue, reviews) | Static analysis and E2E testing |

## Main Areas

**Testing infrastructure and CI**

We already had basic CI checks in Iteration 1. Now CI needs to run all required tests, static analysis, and coverage checks, with reports saved in the repo. James will finish the shared test setup (PR #86) so we can all write integration and API tests the same way.

**Independent client**

This is new. We need a separate client that only uses our public API, so it could work with any service implementing the same endpoints. Multiple instances should be able to run at the same time, and we'll document how different clients and users are identified.

**Google Cloud deployment**

This is new. We'll containerize the service, deploy it to Cloud Run with a Cloud SQL PostgreSQL database, and make sure the mentor can access and use it during the demo. We'll also include the deployment setup and instructions in the repo.

**Static analysis and E2E testing**

Both are new. Vincent will run a bug-finding tool like SpotBugs and save reports before and after we fix the issues. Once the client and cloud service are ready, he'll coordinate E2E testing with a repeatable checklist.

## Shared Work

We'll each continue maintaining our own operations, fixing bugs, and writing tests.

Compared to Iteration 1, we need more complete unit and API tests, equivalence partitions and boundary cases, class and integration tests, and at least **80% branch coverage**. We'll each handle testing and documentation for our own code, using the shared testing setup.

We'll also keep our API docs, GitHub Issues, and README updated, including required reports, AI usage, and contributions.

## Deadlines

- **Dec 7:** Iteration 2 submission (T4)
- **Dec 11:** Second Iteration Demo (T5)

We'll aim to have the client and cloud deployment working early enough to test everything together before submission.