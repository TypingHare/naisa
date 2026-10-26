# Naisa

> This repository contains the team project for the **COMS 4156 Advanced
> Software Engineering** course at Columbia University. Team members include
> James Chen, Vincent Lai, Andrew Hsu, and Emily Wang.

## Testing

Run the complete HTTP API suite from `server` with `make api-test`. It uses
Postman/Newman, a separate PostgreSQL 17 database, and a Java 21 service in Docker.
It covers all 10 HTTP endpoints, checks stored reviews and reports, and requires
at least 55% application branch coverage. See the
[API test instructions](server/api-tests/README.md) for requirements, configuration,
and how to run the collection in Postman.

The saved [HTML branch coverage report](docs/server/coverage/api-tests/index.html)
comes from a live API run. Its supporting files are included in the repository.
It measures application code executed by HTTP requests, including service,
controller, validation, and authorization logic. It does not combine results
from other unit test branches.

Unit tests use JUnit Jupiter, Mockito, and Maven Surefire. Their source is under
`server/src/test/java`. Run `make test` from `server` for the full Java test suite;
`ApplicationTest` requires a configured PostgreSQL database. `make coverage` runs
the Java tests with JaCoCo. Dependencies and Java settings are in `server/pom.xml`,
commands are in `server/Makefile`, and CI is in `.github/workflows/ci.yml`.
