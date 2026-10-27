# Naisa

> This repository contains the team project for the **COMS 4156 Advanced
> Software Engineering** course at Columbia University. Team members include
> James Chen, Vincent Lai, Andrew Hsu, and Emily Wang.

## Project management

Completed Iteration 1 tasks are tracked in [GitHub Issues](https://github.com/TypingHare/naisa/issues?q=is%3Aissue+is%3Aclosed+%22Iteration+1%22).
The records below were created retrospectively after implementation, using the
[contributions document](docs/team-contributions.md), source author tags, and
merged pull requests. Each issue has an assignee, completed-work checklist,
source/PR evidence, and shared-credit notes. All 13 are closed as completed;
this records implementation completion, not final release or access verification.

| Responsible teammate(s) | Completed task record |
| --- | --- |
| James Chen | [#46 - shared HTTP infrastructure and authorization](https://github.com/TypingHare/naisa/issues/46) |
| James Chen | [#47 - client registration, API-key lookup, and health endpoint](https://github.com/TypingHare/naisa/issues/47) |
| James Chen and Vincent Lai | [#48 - shared persistence entities and venue/sport lookup](https://github.com/TypingHare/naisa/issues/48) |
| Vincent Lai | [#49 - addVenue implementation, tests, and documentation](https://github.com/TypingHare/naisa/issues/49) |
| Vincent Lai | [#50 - rateVenue review upserts, tests, and documentation](https://github.com/TypingHare/naisa/issues/50) |
| Vincent Lai | [#51 - double coordinates and smallint-compatible ratings](https://github.com/TypingHare/naisa/issues/51) |
| Vincent Lai | [#52 - venue-sport lighting persistence](https://github.com/TypingHare/naisa/issues/52) |
| Andrew Hsu | [#53 - individual/group searches, alternatives, and venue details](https://github.com/TypingHare/naisa/issues/53) |
| Andrew Hsu | [#54 - unit tests for searches, alternatives, and venue details](https://github.com/TypingHare/naisa/issues/54) |
| Andrew Hsu | [#55 - full API suite, coverage gate, and regression fixes](https://github.com/TypingHare/naisa/issues/55) |
| Emily Wang | [#56 - venue comparison endpoint and ranking behavior](https://github.com/TypingHare/naisa/issues/56) |
| Emily Wang | [#57 - crowd-report submission and current venue status](https://github.com/TypingHare/naisa/issues/57) |
| Emily Wang | [#58 - comparison and crowd service/API tests](https://github.com/TypingHare/naisa/issues/58) |

The task records use this repository's issue tracker, with no external tool.
Before submitting, confirm that the Mentor and Instructor can open the issue
links using their own GitHub accounts. Shared authorship is recorded without
claiming exclusive ownership or inventing work hours or completion dates.

## Setup and run

Use Java 21, Maven through the included wrapper, and PostgreSQL. Follow the
[database setup instructions](docs/server/database.md), including the local
configuration files. PostgreSQL 17 is used by CI and the isolated API suite;
the local setup guide uses PostgreSQL 18.

From `server`, build the application with:

```sh
./mvnw -DskipTests package
```

Start it with `make run`. The default HTTP address is `http://localhost:8080`.
The application applies Flyway migrations at startup. Press Ctrl+C to stop it.
`make test` runs all Java tests and needs both database configurations described
in the setup guide. Use `make unit` for tests that do not need a database.

## API documentation and usage

See the [venue API documentation](docs/server/venue-apis.md) for all seven
proposed operations and the [architecture guide](docs/server/architecture.md)
for the response format. The [additional endpoint documentation](docs/server/README.md)
covers health checks, API key creation, venue creation, and reviews.

First call `POST /v1/clients` without a request body to get an API key. Send
`Authorization: Bearer <apiKey>` with venue requests. `GET /v1/health` and
`POST /v1/clients` do not require a key. Create or load venue, sport, and opening
hour data before searches. Creating a venue alone does not create its sports or
hours. When a search returns an empty list, call the alternatives endpoint with
the changes the client permits. Submit crowd reports before requesting a live
crowd status. The [API test suite](server/api-tests/README.md) provides complete
request sequences and checks successful requests and incorrect use.

### First iteration scope

The MVP implements all seven proposed operations with these changes:

- Clients supply coordinates directly. Venue lookup uses a numeric venue ID;
  lookup by name and address geocoding are not included.
- Individual and group searches share one endpoint. Group radius is required.
  Availability means open at the requested time; it does not confirm a booking.
- Search returns an empty list if no venues match. Clients request alternatives
  separately and use `allowedChanges` to state which constraints can change.
  This keeps the original search constraints explicit.
- Comparison requires coordinates, checks current opening status, sorts by
  distance, and identifies the nearest, cheapest, and least crowded venues.
  The MVP returns these values separately without a combined score, rank, or
  requested comparison date and time.
- Crowd reports use server time and keep earlier reports. Status counts only
  each reporter's latest report, so retained history does not give one reporter
  extra votes. Report creation returns the saved report; status is a separate
  request.
- Crowd status uses the most frequent recent level and breaks ties by report
  time. Age weighting, confidence values, and historical estimates are deferred.
  Without recent reports, the status is `UNKNOWN`.

## Style checks

Run `make lint` from `server`. Checkstyle uses `google_checks.xml` (Google Java
Style). PMD uses [server/.pmd/ruleset.xml](server/.pmd/ruleset.xml): Java code
style rules with five exclusions listed in that file. Plugin versions are set
in [server/pom.xml](server/pom.xml).

## Source disclosure and AI use

The team confirms that no third-party application source code was copied or
adapted. The generated Maven wrapper scripts (`server/mvnw` and `server/mvnw.cmd`)
come from [Apache Maven Wrapper](https://github.com/apache/maven-wrapper) and
retain their Apache License headers. Library dependencies are listed in
[server/pom.xml](server/pom.xml).

See [AI usage](docs/AI-Usage.md) for the team's disclosure. AI author names in
Javadoc identify assisted code, including tests.

## Unit tests

The server uses JUnit Jupiter to run tests, Mockito for repository and clock mocks,
and AssertJ for result assertions. Related tests share setup and teardown in
classes under `server/src/test/java`. Service tests call public methods without
starting Spring or connecting to a database. Each test has independent data.

Run all unit tests with one command from `server`:

```sh
make unit
```

Generate branch coverage for all unit tests available on the current branch:

```sh
make unit-coverage
```

These database-free targets select `*Test` classes and exclude `ApplicationTest`,
`NumericRequestTest`, and `VenueSportPersistenceTest`. All three excluded classes
start Spring and require a database. The shared `UNIT_TESTS` filter is defined in
`server/Makefile`; update it when adding another database-backed test class.
`make test` and `make coverage` include all Java test classes and require a
configured PostgreSQL database. Use `make lint` to run Checkstyle and PMD.

To run only the alternatives tests and generate their coverage:

```sh
make coverage MVN='./mvnw -Dtest=VenueAlternativesTest'
```

The Java version and test dependencies are configured in `server/pom.xml`.
Spring Boot manages the JUnit and Mockito versions and Maven Surefire runs the
tests. The Makefile defines the commands; JaCoCo is configured in the POM and its
agent and report goals are called by the coverage targets. CI configuration is
in `.github/workflows/ci.yml`.

Use Java 21, as configured for the project and CI. If a restricted local Java 25
environment cannot attach Mockito's agent, set `JAVA_TOOL_OPTIONS` for the command:

```sh
JAVA_TOOL_OPTIONS="-javaagent:$HOME/.m2/repository/org/mockito/mockito-core/5.23.0/mockito-core-5.23.0.jar" make unit-coverage
```

The JAR must exist locally and match the Mockito version resolved by Maven.

Open `server/target/site/jacoco/index.html` in a browser to inspect the generated
HTML coverage report. Select a package and class to see method coverage and
missed branches. The report includes only the tests selected for that run.
Files under `server/target/` are local build outputs and are removed by `make clean`.

After the test PRs merge, run `make unit-coverage` to measure combined coverage.
Before course submission, save the combined report in the repository. The
iteration requirement is at least 55% branch coverage across the application;
the second iteration requires at least 80% coverage. API endpoint tests are a
separate course requirement from these service tests.

## API tests

Postman/Newman API tests and fixtures are in [server/api-tests](server/api-tests/).
Run instructions are included with that suite. API tests send real HTTP requests
and are separate from the database-free service unit tests.

## Venue submissions (operations 8 and 9)

- [API contracts, errors, side effects, and step-by-step examples](docs/server/venue-apis.md#operation-8-add-venue)
- [Database setup](docs/server/database.md) and [test/run instructions](docs/server/development.md#testing-venue-submissions-operations-8-and-9)
- Unit tests: `server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java`
  (JUnit Jupiter, Mockito, and Maven Surefire; dependencies configured in `server/pom.xml`).
- API tests: `server/api-tests/naisa-venue-submissions.postman_collection.json`
  (Postman Collection Runner or Newman 6; fixture data in `server/api-tests/seed.sql`).
- Style: Checkstyle using `google_checks.xml` (Google Java Style), including test sources;
  configured in `server/pom.xml`. Run `./mvnw checkstyle:check` from `server`.
- [AI usage disclosure](docs/AI-Usage.md).

The submission tests use Maven-managed libraries and Newman installed through npm;
no third-party implementation code is copied into these tests.

## Submission evidence and team contributions

- [Team contributions](docs/team-contributions.md): per-person summaries and the
  complete source/test `@author` inventory, with shared credit retained.
- [Verification summary](server/reports/venue-submissions/summary.txt): full Java
  tests, focused and complete API tests, database assertions, and coverage.
- [Clean Checkstyle report](server/reports/venue-submissions/checkstyle.xml):
  Google Java Style (`google_checks.xml`), including all production and test Java
  sources. Configuration is in `server/pom.xml`; `make lint` runs the checks.
- [Checked-source hashes](server/reports/venue-submissions/checked-source-sha256.json):
  SHA-256 of the checked Java sources and build/style configuration, normalized
  to UTF-8 with LF endings so the evidence can be matched to a release checkout.
- [Full Java branch coverage](server/reports/venue-submissions/jacoco.xml) and
  [full API HTML coverage](docs/server/coverage/api-tests/index.html).

Build from `server` with `./mvnw package`; run with `make run`. Follow the
[database setup](docs/server/database.md), [development instructions](docs/server/development.md),
and [API contracts](docs/server/venue-apis.md) first. Run `make test`, `make lint`,
and `make api-test` before tagging the final main commit. The saved reports are
development evidence until the reviewed changes are merged and the intended
release contains the same checked sources. The existing `v0.1.0` tag predates
this PR and does not contain these additions.
