# Venue operations

This Spring Boot service has venue search and venue info code.
The submitted [proposal](../proposal.md) records the initial design. This
guide describes the current code.

## Venue routes

All three routes accept JSON with `Content-Type: application/json` and require
an `Authorization: Bearer <api-key>` header. Create a key with
`POST /v1/clients`; the key is returned in `data.apiKey`. The search routes
return a `Capsule<SearchResult>`. The info route returns a `Capsule<VenueInfo>`.

The proposal lists Find Venues and Find Alternatives as Operations 1 and 2.
This server combines them in `POST /v1/venues/search`. The route tries the
requested values first, then tries up to three wider searches. It does not have
a separate Find Alternatives route. The date and time stay fixed; the current
fallback changes only distance and budget. Group search is Operation 3, and
venue info is Operation 4.

- `POST /v1/venues/search` takes a location, sport, date, time, maximum distance
  in kilometres, and budget. It returns all exact matches. If there are none,
  it tries up to three more searches. Each search raises both limits by 5 km
  and USD 5. The sport, date, and time stay fixed. It returns all matches from
  the first successful step. It returns `matchType: "NONE"` and an empty venue
  list if all steps fail. `searchLimitReached` is then `true`.
- `POST /v1/venues/group-search` takes two to 20 member locations, a sport,
  date, time, budget, and an optional distance limit from the group center. It
  returns venues ranked by average member distance. It also returns each
  member's distance and the group center. This route checks the given criteria
  only.
- `POST /v1/venues/info` takes exactly one of `venueId` or `venueName`. The ID
  is the string returned in search results. Name lookup ignores letter case and
  extra space at the start or end. The route returns one venue with its address,
  coordinates, time zone, sports, stored prices, field data, and weekly opening
  hours. A sport's `priceUsd` field is included only when its stored currency
  is `USD`. If a name matches more than one venue, use an ID instead.

## Request examples

Individual search (`POST /v1/venues/search`):

```json
{
  "location": {"latitude": 40.81, "longitude": -73.96},
  "sport": "basketball",
  "date": "2026-10-24",
  "time": "18:00",
  "maxDistance": 5,
  "budget": 0
}
```

Group search (`POST /v1/venues/group-search`):

```json
{
  "locations": [
    {"latitude": 40.81, "longitude": -73.96},
    {"latitude": 40.82, "longitude": -73.95}
  ],
  "sport": "basketball",
  "date": "2026-10-24",
  "time": "18:00",
  "budget": 20,
  "distanceFromCenter": 5
}
```

Venue info (`POST /v1/venues/info`), using an ID from a search result:

```json
{"venueId": "42"}
```

To look up a venue by name, send this body instead:

```json
{"venueName": "Riverside Park"}
```

Send exactly one selector. Name lookup matches the full name, without regard
to letter case or space at the start or end. It is not a partial-name search.

## Search rules

`budget` defaults to zero. A result shows the requested criteria, the criteria
that matched, the changed fields, and the venues. An exact result has
`matchType: "EXACT"`; a result from a later step has `matchType: "ALTERNATIVE"`.
The search model accepts prices stated in USD per court hour. The code checks
weekly opening hours, including overnight hours and the venue's time zone.
Booking status is `UNKNOWN` because the schema has no booking data. A venue
with an unknown price, currency, price unit, time zone, or opening hours cannot
match. Search accepts only `currency = 'USD'` and
`price_unit = 'PER_COURT_HOUR'`. It does not convert other currencies or units.

## Code layout

- `controller/v1/VenueSearchController.java` accepts search and info requests and
  calls the matching service.
- `service/VenueSearchService.java` filters, ranks, and formats results.
  `service/SearchGeometry.java` calculates distances and the group center.
- `service/VenueInfoService.java` checks the venue selector and formats stored
  details. The `VenueInfo*Repository` files read venue, sport, and hours rows.
- `repository/VenueSearchRepository.java` defines the data interface.
  `repository/PostgresVenueSearchRepository.java` reads sports, venue sports,
  venues, and weekly opening periods through JPA in a read-only transaction.
  It loads opening periods in bulk instead of running one query per venue.
- `model/dto/` contains request data and validation rules. `model/data/`
  contains candidate venues and opening hours passed from the repository.
  `model/vo/` contains response data.
- `exception/VenueSearchException.java` and `exception/VenueInfoException.java`
  define operation errors. Unit tests use invented records held only in test
  code. Integration tests use PostgreSQL and roll back their test records.

Search reads stored records from `sports`, `venues`, `venue_sports`, and
`opening_intervals`. Flyway migration V2 adds nullable `currency` and
`price_unit` columns to `venue_sports`, with no defaults or automatic updates
to existing prices. Set those fields only after confirming the price meaning.
Unknown or unsupported metadata excludes the venue from search, including
when its price is zero. A known sport with no eligible venues returns HTTP
200 with an empty venue list. An unknown sport returns HTTP 422 with
`search.unknown_sport`. Database failures remain server errors; they do not
produce empty results. The API key check reads the configured client table.
If catalog rows share a sport name, search returns each venue once with its
lowest eligible price.

The info route reads the current PostgreSQL venue tables through JPA.
Its `surfaceType` value comes from the optional
`venue_sports.surface_type` column. Existing rows can leave this value empty.
Flyway migration V2 adds this column. Hibernate checks the schema at startup.
The info route maps the stored price to `priceUsd` only when `currency` is
`USD`. That field is omitted when currency is unknown or different; the sport
and facility details are still returned. The info route does not convert
currencies or claim a time unit for this price.
The route returns stored weekly hours; it does not report current opening or
booking status. A missing venue returns HTTP 404. A name used by more than one
venue returns HTTP 409. An invalid selector returns HTTP 400.

## Checks

From `server/`, run `make run` to start the server. In another terminal, run
`make test` for tests and `make lint` for Checkstyle and PMD. These targets run
the Maven wrapper. The full test run needs PostgreSQL and
`src/main/resources/database.properties`; see [database setup](./database.md).

Use a dedicated PostgreSQL 18 test database. With `database-test.properties`
configured as described in the setup guide, run:

```bash
SPRING_PROFILES_ACTIVE=test make test
make lint
```

You can also set `SPRING_DATASOURCE_URL` to the test database's JDBC URL. Use
`ddl-auto=validate` as in the example config. Flyway applies the migrations;
Hibernate validates the resulting schema. CI uses the same PostgreSQL version
and schema validation. Repository tests roll back their records. Migration
tests create and drop their own temporary schemas, so the test database user
must be able to create schemas. They check both an empty schema and an upgrade
from V1.
V1 stays unchanged. A database created before Flyway that has no migration
history needs a separate migration plan; do not enable automatic baselining.
