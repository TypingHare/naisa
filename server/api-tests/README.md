# HTTP API tests

This suite sends real HTTP requests through the running application to PostgreSQL.
It uses Postman collection format 2.1 and Newman 6.2.3. No repositories or service
methods are mocked. Related requests run in folder order and share collection
variables created by setup requests. Future dates are calculated at the start
of each complete collection run.

## Run the complete suite

Install Docker, Java 21 or later, Node.js with npm, Python 3, and curl. Start Docker.
From `server`, run:

```sh
make api-test
```

The runner builds the application, creates a separate PostgreSQL 17 database and
Java 21 application container, runs Flyway migrations, loads `seed.sql`, and runs
the collection. It then checks database postconditions from `assertions.sql`.
An exit status of zero means the API assertions, database checks, and the 55%
branch coverage threshold passed. Containers, their test data, and their network
are removed on exit. Each run starts with a new database.

The service uses port 18080 by default. To select another local port:

```sh
API_TEST_PORT=18081 make api-test
```

Only the disposable containers use the test credentials in `run.sh`. The runner
overrides local database configuration and does not connect to the development
database. The fixture SQL must be used only with a disposable test database.

## Cases and endpoint coverage

- `GET /v1/health` and `POST /v1/clients`: public health, empty response body,
  API key issuance, and independent client identities.
- `GET /v1/venues`: individual and group searches, center and member distances,
  result order, budget and radius boundaries, overnight and 24-hour opening
  intervals, empty results, and invalid input.
- `GET /v1/venues/alternatives`: permitted changes, date and time windows,
  unchanged matches, limits, empty results, unknown sports, and invalid requests.
- `GET /v1/venues/{venueId}`: stored details, related lists, sorting, and errors.
- `POST /v1/venues`: creation, read-back, trimming, timezone checks, validation,
  and malformed JSON.
- `POST /v1/venues/{venueId}/reviews`: create and update, user/client/sport scope
  isolation, fractional ratings, and missing or unsupported resources.
- `POST /v1/venues/{venueId}/crowd-reports` and
  `GET /v1/venues/{venueId}/crowd-status`: append-only reports, latest-report
  selection, sport fallback, expiration, aggregation, and errors.
- `GET /v1/venues/comparison`: ordered results, nearest/cheapest/least crowded
  choices, fallback, opening status, and invalid requests.
- Every protected endpoint has a missing-credential case. Additional cases check
  malformed and unknown API keys.

SQL postconditions check stored review updates and trimming, four independent
review scopes, and retention of both reports from the same user. These checks
cover stored behavior for which the API has no separate history endpoint.

## Results and branch coverage

The verified run on 2026-10-26 completed 95 requests and 242 assertions with no
failures. Database postconditions passed. The saved report records 118 covered
branches out of 150 application branches (78.7%).

Newman writes JUnit XML to `server/target/api-tests/results.xml`. Service logs are
in `server/target/api-tests/server.log`. JaCoCo 0.8.14 instruments the Java 21
application with its Java agent. The runner stops that application to flush
execution data and runs `jacoco:report` against the same compiled classes.

Open `server/target/site/jacoco/index.html` for the generated report. Select a
package and class to inspect its missed branches. The runner uses JaCoCo's
application-level `BRANCH` counters, with no custom class exclusions, to enforce
the 55% threshold. Passing runs save the complete HTML report and test results
under `docs/server/coverage/api-tests/` for review and course submission.

The saved report is a snapshot from the API suite alone. It does not claim
combined unit and API coverage. Build outputs under `server/target` are temporary;
the saved HTML report is included in Git. JaCoCo configuration is in
`server/pom.xml`, the runner is `api-tests/run.sh`, and CI calls `make api-test`.

The tests exposed two bugs that this change fixes: comparison treated 24-hour
opening intervals as closed, and review uniqueness omitted the external user ID.
Flyway migration V3 corrects the review constraint without changing migrations
already applied to existing databases.

## Use Postman or Newman with a separate test service

Import `naisa-venue-ops.postman_collection.json` into Postman. Start a service
against a disposable database with migrations applied, then load `seed.sql`
using psql. Set the collection's `baseUrl` and run the entire collection in order.
The same requests can be run from the repository root with:

```sh
npx --yes newman@6.2.3 run server/api-tests/naisa-venue-ops.postman_collection.json
```

This manual mode runs HTTP assertions only. Use `make api-test` for database
postconditions, isolated setup and cleanup, and branch coverage. Do not run two
collections against the same database at the same time.
