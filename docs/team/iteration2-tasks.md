# Iteration 2 — Task Assignments

## Overview

For Iteration 2, we'll continue improving our existing operations while working
on the new requirements.

Each of us will take the lead on one main area, but we'll help each other when
needed. We'll also be responsible for testing, fixing bugs, and documenting our
own operations.

We'll use this document to keep track of our responsibilities and progress, and
GitHub Issues to track individual tasks.

## 1. Team Responsibilities

| Member      | Existing Work                                                               | Iteration 2 Focus                                             |
| ----------- | --------------------------------------------------------------------------- | ------------------------------------------------------------- |
| James Chen  | Core architecture, shared infrastructure, and refactoring                   | Integration testing infrastructure, CI, and final integration |
| Andrew Hsu  | Operations 1–4: venue search, group search, alternatives, and venue details | Independent API client                                        |
| Emily Wang  | Operations 5–7: venue comparison, crowd reporting, and crowd status         | Google Cloud deployment                                       |
| Vincent Lai | Database schema, Operations 8–9: venue creation and reviews                 | Static analysis and E2E testing                               |

## 2. Main Tasks

### James — Testing Infrastructure & CI

- [ ] Finish the integration testing setup (PR #86).
- [ ] Help us decide how we'll run API tests and database integration tests.
- [ ] Update GitHub Actions to run our unit, integration, and API tests.
- [ ] Add static analysis and branch coverage checks to CI.
- [ ] Make sure CI reports are saved in the repo.
- [ ] Set up branch protection for `main` where possible, requiring PR reviews
      and passing CI.
- [ ] Help review and integrate our final changes.

### Andrew — Independent API Client

- [ ] Build a standalone client that communicates with our service through REST
      APIs.
- [ ] Support multiple API operations, including venue search and comparison.
- [ ] Make the service URL configurable.
- [ ] Support multiple client instances running independently.
- [ ] Document how the service distinguishes different clients using API keys
      and `externalUserId`.
- [ ] Write setup and usage instructions for the client.

### Emily — Google Cloud Deployment

- [ ] Set up our Google Cloud environment and confirm billing or course credits.
- [ ] Add deployment configuration to the repo, including a Dockerfile and Cloud
      Run settings.
- [ ] Deploy our Spring Boot service to Google Cloud Run.
- [ ] Work with Vincent to configure Cloud SQL PostgreSQL.
- [ ] Run Flyway migrations on the cloud database.
- [ ] Configure environment variables and secrets.
- [ ] Verify that the deployed APIs work and data is saved correctly.
- [ ] Test the independent client against the deployed service with Andrew.
- [ ] Make sure the mentor can access and use the service during the demo,
      including how to get an API key through `POST /v1/clients`.
- [ ] Document the deployment steps and how to access the service.

### Vincent — Static Analysis & E2E Testing

- [ ] Set up a bug-finding static analysis tool, such as SpotBugs.
- [ ] Generate and commit the static analysis report before fixing issues.
- [ ] Coordinate fixes with the team, then generate and commit the after-fix
      report.
- [ ] Plan and coordinate E2E testing once the client and cloud service are
      ready.
- [ ] Write reproducible E2E test scenarios in `docs/e2e-checklist.md`.
- [ ] Test multiple client instances and workflows involving persistent data.
- [ ] Record E2E test results and link the checklist from our README.
- [ ] Help Emily with cloud database setup and initialization.
- [ ] Discuss and complete the review read path if we agree to include it.

## 3. What We'll All Work On

Along with our main tasks, we'll each continue maintaining our own operations.

- [ ] Finish missing functionality and fix known bugs.
- [ ] Write unit, class-level, and API tests for our operations.
- [ ] Document equivalence partitions and boundary values for each unit and
      endpoint, with a mapping from tests to partitions.
- [ ] Write integration tests for the repositories and database interactions we
      own.
- [ ] Make sure API tests cover multiple clients and persistent data.
- [ ] Help us reach at least 80% branch coverage.
- [ ] Fix static analysis issues in our own code.
- [ ] Update API documentation and explain any changes from our original
      proposal.
- [ ] Document any third-party code we use.
- [ ] Record AI tool usage, prompts, token usage, and AI-assisted code.
- [ ] Create GitHub Issues for our tasks and link related PRs.
- [ ] Review each other's PRs before merging.

## 4. Progress Tracker

| Task                                      | Owner                | Status               |
| ----------------------------------------- | -------------------- | -------------------- |
| Integration testing infrastructure        | James                | In Progress — PR #86 |
| CI and coverage reporting                 | James                | In Progress          |
| Branch protection                         | James                | To Verify            |
| API testing approach                      | Everyone             | To Decide            |
| Independent API client                    | Andrew               | Not Started          |
| Google Cloud deployment                   | Emily                | Not Started          |
| Cloud database setup                      | Emily + Vincent      | Not Started          |
| Static analysis and reports               | Vincent              | Not Started          |
| E2E testing                               | Vincent              | Not Started          |
| Unit, API, and database integration tests | Each operation owner | In Progress          |
| Equivalence partition documentation       | Each operation owner | Not Started          |
| API documentation                         | Each operation owner | In Progress          |
| Final README and contribution records     | Everyone             | Not Started          |

## 5. How We'll Work Together

We can work on most tasks in parallel, but a few things need to happen in order.

- **Testing:** We'll agree on our API testing approach, including whether to use
  MockMvc, Postman, or both. James will finish the shared testing setup, and
  we'll each write tests for our own code.
- **Static analysis:** Vincent will commit the before-fix report first. We'll
  then fix issues in our own code, and Vincent will commit the after-fix report.
  James will add the tool to CI.
- **Cloud:** Emily will lead deployment, with Vincent helping with the database
  and James helping with configuration when needed.
- **Client:** Andrew will build the independent client using our REST APIs.
- **E2E:** Once the client and cloud service are ready, Vincent will coordinate
  E2E testing, and we'll all help verify the main workflows.
- **Documentation:** We'll keep our API documentation and GitHub Issues updated
  as we work, rather than leaving everything until the end.

## 6. Milestones

These are our proposed dates. We'll confirm them once we know the Iteration 2
demo and submission deadlines.

| Milestone                                                        | Target Date  |
| ---------------------------------------------------------------- | ------------ |
| Confirm assignments, testing tools, and GCP billing              | Nov 9        |
| Finish initial static analysis setup and testing infrastructure  | Nov 16       |
| Get the first cloud deployment and client version working        | Nov 23       |
| Connect the client to the deployed service and begin E2E testing | Nov 30       |
| Finish testing, reports, and documentation                       | Dec 4        |
| Final review, merge, and Git tag                                 | Before Dec 7 |

## 7. Final Checklist

Before submitting Iteration 2, we'll make sure that:

- [ ] All planned functionality is complete, or we've documented changes from
      our proposal.
- [ ] Unit and API tests cover the required equivalence partitions and boundary
      values.
- [ ] Test-to-partition mappings are documented.
- [ ] Class-level, internal integration, and external database integration tests
      are complete.
- [ ] API tests cover concurrent clients and persistent data.
- [ ] We've reached at least 80% branch coverage.
- [ ] CI runs all required checks, and CI reports are saved in the repo.
- [ ] Before-and-after static analysis reports are saved in the repo.
- [ ] A clean style checker report is saved for the final tagged version.
- [ ] Branch protection is enabled for `main` where possible.
- [ ] Our service is deployed on Google Cloud and accessible to the mentor.
- [ ] Deployment configuration and instructions are in the repo.
- [ ] Our independent client works with the deployed service.
- [ ] Multi-client E2E tests are completed and documented in
      `docs/e2e-checklist.md`.
- [ ] Our README includes API, client, CI, testing, and deployment instructions.
- [ ] Our README links to GitHub Issues and explains any changes from the
      proposal.
- [ ] Third-party code, AI usage, and individual contributions are documented.
- [ ] All final PRs are reviewed and merged into `main`.
- [ ] We've tagged the final commit for submission.

## 8. Open Questions

We'll confirm these together before finalizing the plan:

1. Should we use MockMvc, Postman, or both for API testing?
2. Should we add a read endpoint for venue reviews?
3. Which GCP account and billing setup will we use? Do we have course credits?

## Notes

We'll update this document and our GitHub Issues as we make progress. We can
adjust assignments if someone's workload changes.

We'll keep a separate record of what each person actually contributed, including
links to relevant PRs and commits.

For the final submission, we'll make sure the required reports, configuration
files, and testing evidence are available in the repo, rather than only
describing them in the README.
