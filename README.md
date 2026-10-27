# Naisa

> This repository contains the team project for the **COMS 4156 Advanced
> Software Engineering** course at Columbia University. Team members include
> James Chen, Vincent Lai, Andrew Hsu, and Emily Wang.

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
