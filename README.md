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
