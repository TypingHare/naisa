# Naisa

> This repository contains the team project for the **COMS 4156 Advanced
> Software Engineering** course at Columbia University. Team members include
> James Chen, Vincent Lai, Andrew Hsu, and Emily Wang.

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
