### Development

This document specifies a set of rules of developing the Spring Boot application
and introduces development-related tools and commands.

#### Javadoc

Please add an `@author` tag to each class or method you add or modify. For
classes in `com.naisa.controller`, `com.naisa.service`, and
`com.naisa.repository`, add the tag to each method you add or modify. For all
other classes in the codebase, add the tag to the class you add or modify.
Separate names in the tag with commas, for example:

```java
/**
 * Description of a class or method.
 *
 * @author James Chen, Andrew Hsu, Vincent Lai, Emily Wang
 */
```

If generative AI tools or agents are used to generate all or part of a method or
class, authors must include their full names along with the names of the tools
or agents they used, separated by commas:

```java
/**
 * Description of a class or method.
 *
 * @author James Chen, Codex
 */
```

The name of each tool or agent must appear immediately after the name of the
author who used it. If two developers use the same AI agent (e.g., Codex), then
the agent's name can appear twice in the `@author` tag. For example:

```java
/**
 * Description of a class or method.
 *
 * @author James Chen, Codex, Vincent Lai, Claude Code, Andrew Hsu, Codex
 */
```

#### Makefile Recipes

The `Makefile` provides several useful targets (commands). Run them with
`make <target-name>` in the `server` workspace. Please ensure that GNU Make 3.8
or later is installed in your environment.

1. `lint`: Run Checkstyle and PMD for linting. Run this command before making a
   pull request.
2. `test`: Run all tests.
3. `coverage`: Run JaCoCo and generate a coverage report.
4. `run`: Start the service.
5. `run-test`: Start the service with the "test" profile active.
6. `download-sources`: Download source files for Java dependencies.

#### Testing Venue Submissions (Operations 8 and 9)

Use Java 21 and the included Maven wrapper. Commands below run from `server`;
on Windows replace `./mvnw` with `.\mvnw.cmd`. Dependency/plugin versions are
configured in `pom.xml`. JUnit Jupiter and Mockito are provided by the Spring
Boot test starter; Maven Surefire runs the tests. Tests are grouped into nested
`AddVenue` and `RateVenue` classes with shared setup and mocked repositories.
Unit tests do not require a running service or database:

```sh
./mvnw -Dtest=VenueSubmissionServiceTest test
```

The service tests check normalization, client attribution, accepted timezone
rules, rejection before writes, repeated creation, whole-venue/sport reviews,
updates preserving identity and creation time, and exact client/user scoping.
Validation annotations and authentication are exercised through real HTTP in the
Postman collection, not by calling the service directly with invalid DTOs.

For all tests, configure a dedicated PostgreSQL 18 database following
[database.md](./database.md). Ensure `database.properties` points to that test
database, since `ApplicationTest` uses the default profile. Do not configure
`ddl-auto=create-drop`: Flyway owns the schema and its constraints and triggers.
Use the example's `ddl-auto=validate` setting. Then run:

```sh
./mvnw test
./mvnw package
./mvnw spring-boot:run
```

For the API suite, keep the service running, then apply the existing fixture
against **the same disposable test database**:

```sh
psql -h localhost -U YOUR_TEST_USER -d YOUR_TEST_DATABASE -v ON_ERROR_STOP=1 -f api-tests/seed.sql
```

This fixture writes IDs in the 9000 range and removes crowd reports on those
fixture venues. It must not run against a database containing valuable data.
The submission suite does not require the operations 5-7 collection to run first.

Import `api-tests/naisa-venue-submissions.postman_collection.json` into Postman
and run the entire collection, in order, using Collection Runner. Alternatively,
install Node.js/npm and run Newman 6:

```sh
npx --yes newman@6 run api-tests/naisa-venue-submissions.postman_collection.json \
  --env-var baseUrl=http://localhost:8080 \
  --reporters cli,junit --reporter-junit-export target/submission-api-tests.xml
```

The collection contains its own configuration: `baseUrl`, `offeredVenueId`
(9001), `offeredSportId` (9001), and `unsupportedSportId` (9002). Environment
variables can override these defaults. Each full run issues two new API keys,
creates fresh venues, and checks independent users, clients, venues, and sport
scopes. Run all folders in order; later requests use IDs captured by earlier
requests. Running one folder alone requires those variables to have been set.

Assertions check success/error statuses and error codes, persisted venue fields,
review ID stability on updates, and distinct IDs for independent review scopes.
The collection also checks field boundaries, missing fields, malformed JSON,
invalid timezone/sport/venue IDs, and authorization failures. It leaves its
clients, venues, and reviews in the test database; discard the disposable
database after testing. HTTP alone cannot inspect stored review comments or
timestamps. After the collection finishes, copy its `createdVenueId` variable
(the ID returned by "Create a venue") and run the supplemental SQL checks:

```sh
psql -h localhost -U YOUR_TEST_USER -d YOUR_TEST_DATABASE \
  -v venue_id=123 -f api-tests/submission-assertions.sql
```

Replace 123 with that run's venue ID. These checks verify stored review content,
user/client/sport isolation, timestamps, and that invalid requests did not add
extra records. They create only a temporary context table and roll back at the
end. They assume the default fixture venue and sport IDs (9001). A failed
assertion terminates `psql` with a nonzero status.

To check style, including test sources, and generate coverage:

```sh
./mvnw checkstyle:check
./mvnw pmd:check -Dpmd.printFailingErrors=true
./mvnw jacoco:prepare-agent test jacoco:report
```

Checkstyle uses `google_checks.xml`; PMD uses `.pmd/ruleset.xml`. Test reports
are in `target/surefire-reports`, Checkstyle XML in `target/checkstyle-result.xml`,
and JaCoCo HTML/XML in `target/site/jacoco`. These generated files are ignored by
Git. A submission must explicitly preserve required final reports outside
`target`; this focused suite alone does not establish whole-project 55% coverage.
The verified development snapshot for this change is preserved in
`server/reports/venue-submissions/` (repository-relative), including a summary,
full Java-suite JaCoCo XML, separate API-only JaCoCo XML, clean Checkstyle XML,
and both Newman JUnit reports. The saved full Java suite exceeds 55% branch
coverage; the focused submission tests cover both target methods' branches.
`checked-source-sha256.json` records SHA-256 hashes of the checked Java sources
and build/style configuration, normalized to UTF-8 text with LF endings.
Verify those files match the final release checkout, and rerun and replace
the reports after any source/configuration changes before tagging a release.
