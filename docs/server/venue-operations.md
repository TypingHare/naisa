# Venue operations

This Spring Boot service has venue search and venue info code.
The submitted [proposal](../proposal.md) records the initial design. This
guide describes the current code.

## Venue routes

All three routes require an API key and accept JSON. The search routes return a
`Capsule<SearchResult>`. The info route returns a `Capsule<VenueInfo>`.

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
  hours. The response labels prices as USD. If a name matches more than one
  venue, use an ID instead.

For example, the individual search body is:

```json
{
  "location": {"latitude": 40.81, "longitude": -73.96},
  "sport": "basketball",
  "date": "2026-10-17",
  "time": "18:00",
  "maxDistance": 5,
  "budget": 0
}
```

`budget` defaults to zero. A result shows the requested criteria, the criteria
that matched, the changed fields, and the venues. An exact result has
`matchType: "EXACT"`; a result from a later step has `matchType: "ALTERNATIVE"`.
The search model accepts prices stated in USD per court hour. The code checks
weekly opening hours, including overnight hours and the venue's time zone.
Booking status is `UNKNOWN` because the schema has no booking data. A venue
with an unknown price, price unit, time zone, or opening hours cannot match.

## Code layout

- `controller/v1/VenueSearchController.java` accepts search and info requests and
  calls the matching service.
- `service/VenueSearchService.java` filters, ranks, and formats results.
  `service/SearchGeometry.java` calculates distances and the group center.
- `service/VenueInfoService.java` checks the venue selector and formats stored
  details. The `VenueInfo*Repository` files read venue, sport, and hours rows.
- `repository/VenueSearchRepository.java` defines the data interface.
  `repository/PlaceholderVenueSearchRepository.java` marks the database work
  that is still required.
- `model/dto/` contains request data and validation rules. `model/data/`
  contains candidate venues and opening hours passed from the repository.
  `model/vo/` contains response data.
- `exception/VenueSearchException.java` and `exception/VenueInfoException.java`
  define operation errors. The search tests use invented records held only in
  test code.

The placeholder search repository does not return invented venues. It must
later read `sports`, `venues`, `venue_sports`, and `opening_intervals` from the
database and fill `VenueSearchData`. The schema describes prices as dollars,
but it does not store a currency or a price unit. The search adapter must
confirm both before it can report prices in USD per court hour. Until then, a
valid search request returns HTTP 501 with
`search.data_source_not_implemented`. The API key check needs the
configured client database table.

The info route reads the current PostgreSQL venue tables through JPA. It does
not use the search placeholder. Its `surfaceType` value comes from the optional
`venue_sports.surface_type` column. Existing rows can leave this value empty.
Flyway migration V2 adds this column. Hibernate checks the schema at startup.
The info route maps each stored dollar price to `priceUsd`. This uses the
assumption that the stored dollars are USD; the schema has no currency field.
The route returns stored weekly hours; it does not report current opening or
booking status. A missing venue returns HTTP 404. A name used by more than one
venue returns HTTP 409. An invalid selector returns HTTP 400.

## Checks

From `server/`, run `make run` to start the server. In another terminal, run
`make test` for tests and `make lint` for Checkstyle and PMD. These targets run
the Maven wrapper. The full test run needs PostgreSQL and
`src/main/resources/database.properties`; see [database setup](./database.md).
