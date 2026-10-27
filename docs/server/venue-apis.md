### Venue APIs

This document describes the API contracts for Operations 1-9. All
endpoints require authorization and use the standard `Capsule` response format.
See [architecture.md](./architecture.md) for details.

Coordinates use double-precision values and must be finite and within the stated
latitude and longitude ranges, including coordinates inside group locations.
JSON fields representing integers reject decimal tokens instead of truncating
them. In particular, venue ratings must be integer tokens from 1 through 5;
fractional, decimal-form (for example, `4.0`), and overflowing ratings return
HTTP 400 with `request.invalid_body`. Missing ratings and integers outside
1 through 5 return HTTP 400 with `request.validation_failed`.

### Operations 1 and 3: Find Venues

`GET /v1/venues`

This endpoint finds open venues for one person or a group. Both searches use
query parameters and return the same response format.

#### Request

The following query parameters are required:

- `sportId`: The ID of the requested sport
- `date`: The desired date in YYYY-MM-DD format, for example 2026-10-23.
- `time`: The desired time on a 24-hour clock. Accept HH:mm, HH:mm:ss, or
  HH:mm:ss with 1â€“9 fractional second digits. Examples: 14:30, 14:30:45, and
  14:30:45.123. Hours range from 00 to 23. Time-zone offsets are not accepted.
- `maxDistance`: Maximum distance in kilometers from the search center. This
  parameter is required for both single-member and group searches.

Supply locations in exactly one of these forms:

- A single `latitude` between -90 and 90 and `longitude` between -180 and 180.
- An indexed list of 1 to 10 locations. Each index requires
  `locations[i].latitude` between -90 and 90 and `locations[i].longitude`
  between -180 and 180. Start at index 0 and use consecutive indices.

Do not mix a single coordinate pair with indexed locations. A one-member list
has the same meaning as a single coordinate pair. Input order identifies the
members in the response.

The following query parameter is optional:

- `budget`: Hourly budget for hourly venue rental costs. Defaults to 0.

#### Request Examples

Single origin:

```http
GET /v1/venues?latitude=40.81&longitude=-73.96&sportId=1&date=2026-11-01&time=18:30&maxDistance=2&budget=20
```

Two members (square brackets must be URL-encoded when required by the client):

```http
GET /v1/venues?locations[0].latitude=40.81&locations[0].longitude=-73.96&locations[1].latitude=40.82&locations[1].longitude=-73.95&sportId=1&date=2026-11-01&time=18:30&maxDistance=2&budget=20
```

#### Response

HTTP 200:

```json
{
    "message": "Venues found.",
    "data": [
        {
            "venueId": 1,
            "name": "Venue A",
            "distanceKm": 0.5,
            "averageDistanceKm": 0.5,
            "memberDistancesKm": [0.5],
            "hourlyPrice": 20.0,
            "isIndoor": true,
            "available": true
        }
    ]
}
```

Each result contains the venue ID, name, hourly price in dollars, indoor status
for the requested sport, and availability at the requested date and time.

- `distanceKm` is the distance from the search center to the venue.
- `memberDistancesKm` contains each member's distance to the venue in input order.
- `averageDistanceKm` is the arithmetic average of the member distances.

All distances are in kilometers. With one member, `distanceKm`,
`averageDistanceKm`, and the single member distance are equal. Sort results by
`averageDistanceKm` ascending, then by venue ID ascending. Every returned venue
is open, so `available` is always `true`.

If no venues match, return HTTP 200 with an empty list:

```json
{
    "message": "No venues match the search parameters.",
    "data": []
}
```

#### Behaviors

Validate the search parameters and check that the requested sport exists. The
sport ID must be a positive integer. Require one complete coordinate pair or
1 to 10 complete indexed locations, with no mixture of the two forms.
Coordinates must be within their specified ranges. `maxDistance` and `budget`
must be finite and nonnegative. Since all venues are in New York City, we interpret the requested date and time in
`America/New_York`. Reject the request with HTTP 400 and
`request.validation_failed` if its combined date and time is earlier than the
current date and time. This includes an earlier time today.

Calculate the search center as the arithmetic average of the member latitudes
and the arithmetic average of the member longitudes. For one member, the center
is that member's location.

Select venues that offer the requested sport. Use the Haversine formula to
calculate direct geographic distances from the center and from each member to
each venue. Include venues whose center-to-venue distance is less than or equal
to `maxDistance`. The radius does not limit each member's distance or the average
member distance.

Use the stored price for the requested sport as `hourlyPrice`. Include venues
whose hourly price is less than or equal to `budget`. If `budget` is omitted,
use 0, which includes only free offerings.

Use New York local time to check the venue's weekly opening intervals for the
requested date and time:

- Include the opening time and exclude the closing time.
- If the closing time is earlier than the opening time, the interval continues
  into the next day. For example, Monday 22:00â€“02:00 includes Tuesday 01:00.
- Equal opening and closing times represent a 24-hour interval that starts at
  the opening time on the specified weekday.
- Multiple intervals on the same day are permitted. A match with any interval is
  sufficient.
- Exclude venues with no opening intervals.

Availability means that the venue is open at the requested date and time.
Booking availability and crowd levels are not checked. The search does not
require the venue to remain open for an activity duration. Closing-soon warnings
are deferred.

Return an empty list when no venues satisfy all filters. Do not call
`findAlternatives` in this iteration.

#### Errors

| Status | `errorCode`                                  | When                          |
| ------ | -------------------------------------------- | ----------------------------- |
| 400    | `request.validation_failed`                  | Missing or invalid parameters |
| 401    | `authorization.no_api_key_provided`          | Missing authorization header  |
| 401    | `authorization.invalid_authorization_header` | Invalid Bearer header         |
| 401    | `client.invalid_api_key`                     | Unknown API key               |
| 404    | `sport.not_found`                            | Sport does not exist          |

Example invalid parameters: invalid dates, times outside the accepted 24-hour
formats, a requested date and time in the past, out-of-range coordinates,
nonpositive sport IDs, negative or nonfinite distance or budget values, missing
location coordinates, more than 10 members, and mixed location forms.

#### Deferred Behavior/Edge Case

- DEFERRED - Closing soon warning: for times that are within 1 hour of venue
  close, future behavior should include a warning.
    - Current behavior: no warning
    - Design decisions: warning threshold and response decision

### Operation 2: Find Alternatives

`GET /v1/venues/alternatives`

This endpoint suggests venues by changing only the search values permitted by
the client. Operation 1 continues to return an empty list when no venues match;
it does not call this endpoint automatically.

#### Request

Use the same search query parameters, formats, and validation rules as
operation 1:

- Required: `latitude`, `longitude`, `sportId`, `date`, `time`, and
  `maxDistance`.
- Optional: `budget`, which defaults to 0 dollars per hour.

The additional required query parameter is:

- `allowedChanges`: A nonempty, comma-separated set of `DATE`, `TIME`,
  `MAX_DISTANCE`, or `BUDGET`. Values are case-sensitive. Repeated values
  represent the same permission. Values not listed must remain unchanged.

The original requested date and time must not be in the past in
`America/New_York`. Seconds and fractional seconds are accepted, as in
operation 1.

Example:

```http
GET /v1/venues/alternatives?latitude=40.81&longitude=-73.96&sportId=1&date=2026-11-01&time=18:30&maxDistance=2&budget=10&allowedChanges=TIME,MAX_DISTANCE
```

#### Response

HTTP 200:

```json
{
    "message": "Alternatives found.",
    "data": [
        {
            "venue": {
                "venueId": 1,
                "name": "Venue A",
                "distanceKm": 3.5,
                "hourlyPrice": 8.00,
                "isIndoor": true,
                "available": true
            },
            "date": "2026-11-01",
            "time": "19:30:00",
            "maxDistance": 3.5,
            "budget": 10.00
        }
    ]
}
```

Each entry contains the existing operation 1 venue result under `venue`, plus
its suggested date, time, radius, and hourly budget. A venue may appear more
than once with different dates or times. Return at most 10 entries.

If nearby offerings exist but no permitted alternative qualifies, return
HTTP 200:

```json
{
    "message": "No alternatives match the permitted changes.",
    "data": []
}
```

#### Behaviors

Apply each of the following rules only when its constraint is listed in
`allowedChanges`:

- `DATE`: Check up to 3 calendar days before or after the requested date,
  including the requested date.
- `TIME`: Check the requested time and offsets of -2, -1, +1, and +2 hours
  on each permitted date. Preserve minutes, seconds, and fractional seconds.
- `MAX_DISTANCE`: Permit up to 5 additional kilometers beyond the original
  radius.
- `BUDGET`: Permit up to 20 additional dollars per hour beyond the original
  hourly budget.

Discard all suggested dates and times that are in the past. Time changes may
cross midnight only if `DATE` is also permitted and the resulting date stays
within the 3-day limit. Apply operation 1's opening-hour rules, including
opening and closing boundaries, overnight intervals, and 24-hour intervals.

Keep the original radius and budget unless an increase is necessary. When an
increase is needed, suggest only the calculated venue distance or stored
hourly price required to include that venue, within the permitted limits.

Include venues that satisfy the original request without changes. Remove
duplicate venue/date/time combinations. Sort by:

1. Number of changed constraints, fewest first.
2. Absolute difference from the original requested date and time, smallest
   first.
3. Suggested date and time, earlier first.
4. Distance, shortest first.
5. Hourly price, lowest first.
6. Venue ID, lowest first.

Count actual changes rather than the number of permissions. Return the first
10 entries after sorting.

Determine whether a nearby offering exists before applying budget and
opening-hour filters. If no offering for the sport is within the permitted
radius, return the no-nearby-venues error. If a nearby offering exists but no
alternative passes the remaining filters, return an empty list.

#### Errors

| Status | `errorCode`                                  | When                                 |
| ------ | -------------------------------------------- | ------------------------------------ |
| 400    | `request.validation_failed`                  | Missing or invalid parameters        |
| 401    | `authorization.no_api_key_provided`          | Missing authorization header         |
| 401    | `authorization.invalid_authorization_header` | Invalid Bearer header                |
| 401    | `client.invalid_api_key`                     | Unknown API key                      |
| 404    | `sport.not_found`                            | Sport does not exist                 |
| 404    | `venue.no_nearby_venues`                      | No sport offering within the radius  |

Invalid parameters include an empty or unknown `allowedChanges` value and the
invalid search values listed for operation 1. A past original date and time
is rejected even when `DATE` or `TIME` changes are permitted.

The `venue.no_nearby_venues` message is:

> There are no venues for this sport nearby

### Operation 4: Get Venue Info

`GET /v1/venues/{venueId}`

This endpoint returns the stored details of one venue, including its location,
offered sports, and weekly opening intervals.

#### Request

Supply one numeric venue ID in the path. The value must fit a signed 64-bit
integer. No query parameters or request body are required.

Example:

```http
GET /v1/venues/1
```

#### Response

HTTP 200:

```json
{
    "message": "Venue information retrieved.",
    "data": {
        "venueId": 1,
        "name": "Venue A",
        "address": "123 Example Street, New York, NY",
        "latitude": 40.81,
        "longitude": -73.96,
        "timezone": "America/New_York",
        "sports": [
            {
                "sportId": 1,
                "name": "Basketball",
                "hourlyPrice": 20.00,
                "isIndoor": true
            }
        ],
        "openingIntervals": [
            {
                "dayOfWeek": "MONDAY",
                "opensAt": "09:00:00",
                "closesAt": "21:00:00"
            }
        ]
    }
}
```

The response contains one venue object. Coordinates are in degrees. Each sport
entry contains its ID, name, hourly price in dollars, and indoor status at this
venue. Sort sports by sport ID ascending.

Opening intervals apply to the whole venue. Return weekday names in uppercase,
from `MONDAY` through `SUNDAY`, and local times in ISO format. Interpret these
times in the returned `timezone`. Sort intervals by weekday, Monday first,
then by opening time and closing time ascending.

#### Behaviors

Look up the venue by its ID. Return HTTP 404 with `venue.not_found` when the
venue does not exist. Names are not unique and are not lookup identifiers for
this endpoint.

Return all offered sports and all weekly opening intervals. A venue can be
retrieved even when it is closed. If it has no sports or opening intervals,
return HTTP 200 with `sports: []` or `openingIntervals: []`, respectively.

Include the opening time and exclude the closing time when interpreting an
interval. A closing time earlier than the opening time is on the next day.
Equal opening and closing times represent 24 hours from the stated opening
time. Preserve multiple intervals on the same weekday.

#### Errors

| Status | `errorCode`                                  | When                              |
| ------ | -------------------------------------------- | --------------------------------- |
| 400    | `request.invalid_parameter`                  | Venue ID is not a 64-bit integer  |
| 401    | `authorization.no_api_key_provided`          | Missing authorization header      |
| 401    | `authorization.invalid_authorization_header` | Invalid Bearer header             |
| 401    | `client.invalid_api_key`                     | Unknown API key                   |
| 404    | `venue.not_found`                            | Venue does not exist              |

### Operation 5: Compare Venues

`GET /v1/venues/comparison`

This endpoint compares 2â€“5 venues for a given sport. Venues are sorted by
distance from the user's location.

##### Request

The following query parameters are required:

- `venueIds`: 2â€“5 distinct venue IDs, separated by commas.
- `sportId`: The ID of the requested sport.
- `latitude`: The user's latitude, between -90 and 90.
- `longitude`: The user's longitude, between -180 and 180.

Example:

```http
GET /v1/venues/comparison?venueIds=1,2&sportId=1&latitude=40.81&longitude=-73.96
```

##### Response

HTTP 200:

```json
{
    "message": "Venues compared.",
    "data": {
        "venues": [
            {
                "venueId": 1,
                "name": "Venue A",
                "distanceKm": 0.5,
                "hourlyPrice": 20,
                "isIndoor": true,
                "openNow": true,
                "crowdLevel": "LIGHT"
            },
            {
                "venueId": 2,
                "name": "Venue B",
                "distanceKm": 1.2,
                "hourlyPrice": 10,
                "isIndoor": false,
                "openNow": false,
                "crowdLevel": "BUSY"
            }
        ],
        "nearestVenueId": 1,
        "cheapestVenueId": 2,
        "leastCrowdedVenueId": 1
    }
}
```

##### Behavior

We first check that all venue IDs are distinct and that each venue offers the
requested sport. Duplicate IDs or unsupported sports are rejected.

For each venue, we return its name, distance, price, indoor status, opening
status, and crowd level. The crowd level is determined using the requested sport
and the fallback behavior described in Operation 7.

We use the venue's local time and opening hours to determine `openNow`. If the
opening status cannot be determined, we return `null`.

The venues are sorted by distance, nearest first. `nearestVenueId`,
`cheapestVenueId`, and `leastCrowdedVenueId` identify the venues with the
shortest distance, lowest price, and lowest crowd level. Ties are broken by the
lowest venue ID.

A venue without recent crowd reports has `crowdLevel: null` and is not
considered for `leastCrowdedVenueId`. If no venue has recent crowd reports,
`leastCrowdedVenueId` is `null`.

For the first iteration, we compare these values separately instead of
calculating an overall weighted score. The user's coordinates are required.

##### Errors

| Status | `errorCode`                                  | When                                |
| ------ | -------------------------------------------- | ----------------------------------- |
| 400    | `request.validation_failed`                  | Invalid parameters or duplicate IDs |
| 400    | `venue.sport_not_offered`                    | A venue does not offer the sport    |
| 401    | `authorization.no_api_key_provided`          | Missing authorization header        |
| 401    | `authorization.invalid_authorization_header` | Invalid Bearer header               |
| 401    | `client.invalid_api_key`                     | Unknown API key                     |
| 404    | `venue.not_found`                            | Venue does not exist                |
| 404    | `sport.not_found`                            | Sport does not exist                |

### Operation 6: Report Venue Status

`POST /v1/venues/{venueId}/crowd-reports`

This endpoint allows a client to submit a crowd report for a venue.

##### Request

The venue ID is provided in the path. The request body contains:

```json
{
    "externalUserId": "user123",
    "crowdLevel": "BUSY",
    "sportId": 1
}
```

`externalUserId` and `crowdLevel` are required. `sportId` is optional.

The supported crowd levels are `EMPTY`, `LIGHT`, `MODERATE`, `BUSY`, and
`PACKED`.

##### Response

HTTP 201:

```json
{
    "message": "Crowd report saved.",
    "data": {
        "reportId": 1,
        "crowdLevel": "BUSY",
        "createdAt": "2026-10-20T18:30:00Z"
    }
}
```

##### Behavior

We validate the venue, crowd level, and optional sport before saving the report.
If `sportId` is provided, the venue must offer that sport. An unknown sport ID
is treated as not offered.

A reporter is identified by the authenticated client and `externalUserId`. Every
submission creates a new report with the current server time, and earlier
reports are kept. When estimating crowd status, Operation 7 counts only each
reporter's most recent report.

If `sportId` is omitted, the report applies to the venue as a whole.

##### Errors

| Status | `errorCode`                                  | When                           |
| ------ | -------------------------------------------- | ------------------------------ |
| 400    | `request.validation_failed`                  | Missing or invalid fields      |
| 400    | `venue.sport_not_offered`                    | Venue does not offer the sport |
| 401    | `authorization.no_api_key_provided`          | Missing authorization header   |
| 401    | `authorization.invalid_authorization_header` | Invalid Bearer header          |
| 401    | `client.invalid_api_key`                     | Unknown API key                |
| 404    | `venue.not_found`                            | Venue does not exist           |

### Operation 7: Get Venue Status

`GET /v1/venues/{venueId}/crowd-status`

This endpoint returns the crowd level of a venue based on recent reports.

##### Request

The venue ID is required in the path. An optional `sportId` query parameter can
be used to get the status for a specific sport.

Examples:

```http
GET /v1/venues/1/crowd-status
GET /v1/venues/1/crowd-status?sportId=1
```

##### Response

HTTP 200:

```json
{
    "message": "Crowd status retrieved.",
    "data": {
        "venueId": 1,
        "crowdLevel": "BUSY",
        "reportCount": 3,
        "latestReportAt": "2026-10-20T18:30:00Z",
        "source": "LIVE"
    }
}
```

##### Behavior

We use reports submitted within the past two hours to determine the current
crowd level.

Each reporter (identified by the client and `externalUserId`) is counted once,
using their most recent report within this window.

If `sportId` is provided, we first use recent reports for that sport. If none
are available, we use all recent reports for the venue instead.

If `sportId` is omitted, we also use all recent reports for the venue. If a
sport is specified, the venue must offer it. An unknown sport ID is treated as
not offered.

The crowd level, report count, and latest report time are calculated from the
selected reports.

The crowd level is the most frequently reported level. If multiple levels have
the same count, we use the level from the most recent report among them.

When recent reports are available, `source` is `LIVE`. If there are no recent
reports, we return `crowdLevel: null`, `reportCount: 0`, `latestReportAt: null`,
and `source: "UNKNOWN"`.

Historical estimates are not included in the first iteration.

##### Errors

| Status | `errorCode`                                  | When                               |
| ------ | -------------------------------------------- | ---------------------------------- |
| 400    | `request.invalid_parameter`                  | Non-numeric `venueId` or `sportId` |
| 400    | `venue.sport_not_offered`                    | Venue does not offer the sport     |
| 401    | `authorization.no_api_key_provided`          | Missing authorization header       |
| 401    | `authorization.invalid_authorization_header` | Invalid Bearer header              |
| 401    | `client.invalid_api_key`                     | Unknown API key                    |
| 404    | `venue.not_found`                            | Venue does not exist               |

### Operation 8: Add Venue

`POST /v1/venues`

Creates a venue attributed to the authenticated client. Send
`Authorization: Bearer <apiKey>` and `Content-Type: application/json`.
Obtain a key first with `POST /v1/clients` (no body or authorization required);
its HTTP 201 response contains the key in `data.apiKey`.

#### Request

All five fields are required and must be non-null:

| Field | Type | Rules |
| --- | --- | --- |
| `name` | string | Nonblank; at most 255 characters |
| `address` | string | Nonblank; at most 255 characters |
| `latitude` | number | Inclusive range -90 to 90 |
| `longitude` | number | Inclusive range -180 to 180 |
| `timezone` | string | Nonblank; at most 32 characters; recognized Java region ID, such as `America/New_York` or `UTC` |

```json
{
  "name": "Riverside Court",
  "address": "1 Riverside Drive, New York, NY",
  "latitude": 40.812345,
  "longitude": -73.961234,
  "timezone": "America/New_York"
}
```

There is no external-user field on venue submissions. The client identity comes
from the API key, not the request body. Length validation runs before surrounding
whitespace is stripped from the name, address, and timezone. Coordinates use Java `Double` values and are
stored as PostgreSQL `DOUBLE PRECISION` values; no address geocoding is performed.
Timezone names are case-sensitive; a fixed offset such as `+02:00` is rejected.

#### Response and side effects

HTTP 201:

```json
{"message":"Venue created.","data":{"id":123}}
```

The returned ID is database-generated. One venue row is inserted, including the
submitting client and a database-generated creation timestamp. Repeating the same
request creates a separate venue; names and addresses are not deduplicated.

The operation creates no sports, prices, or opening intervals. Consequently, a
new venue can be retrieved with `GET /v1/venues/123` and reviewed immediately,
but will not appear in sport searches until an operator supplies its sport
offerings and opening hours in the database. There is no public endpoint for
adding these offerings or hours in this iteration. Test fixtures provide them.

### Operation 9: Rate Venue

`POST /v1/venues/{venueId}/reviews`

Creates or updates a review for an existing venue. Requires the same authorization
and JSON content-type headers as operation 8. `venueId` is a signed 64-bit integer
identifying an existing venue; an unknown ID returns 404.

#### Request

| Field | Type | Rules |
| --- | --- | --- |
| `externalUserID` | string | Required, nonblank, at most 128 characters; spelling and capitalization are exact |
| `rating` | number | Required; integer representation from 1 through 5; decimal tokens (including `4.0`) are rejected during deserialization |
| `comment` | string | Required, nonblank, at most 2,000 characters |
| `sportId` | integer or null | Optional; if supplied, must be positive and offered at this venue |

```json
{
  "externalUserID": "member-42",
  "rating": 5,
  "comment": "Clean court with plenty of space."
}
```

Omit `sportId` or set it to `null` to review the whole venue. For a sport-specific
review, add its numeric ID, for example `"sportId": 9001`. A registered sport
that is not offered at the selected venue is rejected, just like an unknown sport.

`externalUserID` is the calling client's user identifier. Naisa does not register
or authenticate the external user. Identifiers are preserved exactly, including
case and surrounding whitespace. `member-42` and ` member-42 ` identify different
users. Comments are stripped of surrounding whitespace after length validation.

#### Response and side effects

HTTP 200 for both creation and update:

```json
{"message":"Review saved.","data":{"id":456}}
```

Ratings use Java `Short` and PostgreSQL `SMALLINT`, with validation restricting
them to 1-5. Prices and budgets retain their decimal types.

A review is identified by the tuple `(client, externalUserID, venue, sport)`.
Repeating this tuple replaces the rating and comment and returns the same ID;
the creation timestamp is retained and the update timestamp is refreshed by the
database. Different clients, external users, venues, and sport scopes have
independent reviews. The whole-venue scope is distinct from every sport scope.
There is no review-retrieval endpoint in this iteration; the response contains
only the saved review ID, not the rating or comment.

The service locks the venue row before looking up the review and holds the lock
until the transaction ends. Writes to the same venue are serialized. The database
also enforces uniqueness, including the external user and treating a null sport
as one scope. Migration `V4__include_external_user_in_review_scope.sql` fixes the
older constraint that incorrectly allowed only one user per client/venue/sport.

### Errors for Operations 8 and 9

Errors use the shared capsule, for example:

```json
{"message":"Request validation failed.","errorCode":"request.validation_failed"}
```

Clients should branch on `errorCode` and HTTP status, rather than matching
human-readable messages. Both operations
validate the body before executing business logic. Rejected requests do not
create or modify venues or reviews.

| Status | `errorCode` | Applies to / cause |
| --- | --- | --- |
| 400 | `request.invalid_body` | Both: missing/malformed JSON or a value that cannot be deserialized; rating decimal tokens or values outside SMALLINT also fail here |
| 400 | `request.validation_failed` | Both: missing/null required fields, blank text, excessive length, or a constraint violation |
| 400 | `venue.invalid_timezone` | Add venue: unrecognized timezone after trimming |
| 400 | `request.invalid_parameter` | Rate venue: path venue ID cannot be parsed as a 64-bit integer |
| 400 | `venue.sport_not_offered` | Rate venue: specified sport is absent from the venue |
| 401 | `authorization.no_api_key_provided` | Both: missing Authorization header |
| 401 | `authorization.invalid_authorization_header` | Both: malformed header, including a missing `Bearer ` prefix |
| 401 | `client.invalid_api_key` | Both: API key is not recognized |
| 404 | `venue.not_found` | Rate venue: venue ID does not exist |
| 500 | `server.internal_error` | Both: unexpected application or persistence failure |

These are the supported application-level outcomes for POST requests to these
routes. Unsupported methods or media types are not part of these contracts;
the current catch-all exception handler may translate framework errors into 500.

### Submission Walkthrough and Bad Usage

Use a running service and a disposable test database. These examples use a POSIX
shell and `curl`; in Windows PowerShell use `curl.exe` with equivalent quoting,
or use the provided Postman collection.

1. Request a client key:

   ```sh
   curl -X POST http://localhost:8080/v1/clients
   ```

   Copy `data.apiKey` into `API_KEY` in your shell.

2. Create a venue:

   ```sh
   curl -X POST http://localhost:8080/v1/venues \
     -H "Authorization: Bearer $API_KEY" -H 'Content-Type: application/json' \
     -d '{"name":"Riverside Court","address":"1 Riverside Drive","latitude":40.812345,"longitude":-73.961234,"timezone":"America/New_York"}'
   ```

   Save `data.id` as `VENUE_ID`, then retrieve it using
   `GET /v1/venues/{venueId}` with the same Authorization header.

3. Review the whole venue:

   ```sh
   curl -X POST "http://localhost:8080/v1/venues/$VENUE_ID/reviews" \
     -H "Authorization: Bearer $API_KEY" -H 'Content-Type: application/json' \
     -d '{"externalUserID":"member-42","rating":5,"comment":"Excellent court."}'
   ```

   Repeat with rating 3 and a changed comment. HTTP 200 returns the same review
   ID. Use a different `externalUserID` to create a separate review.

4. To review an offered sport, load the API test seed described in
   [development.md](./development.md#testing-venue-submissions-operations-8-and-9),
   then post to `/v1/venues/9001/reviews` with `"sportId":9001`. Use a fresh client
   key for each demo run. This sport review is independent of a whole-venue review
   by the same user at venue 9001.

Examples that should fail:

- Rating a venue before it exists: 404 `venue.not_found`.
- Sending `externalUserId` instead of `externalUserID`: 400
  `request.validation_failed` because the required field is missing.
- Sending rating 2.5, 4.0, or a value outside SMALLINT: 400 `request.invalid_body`.
- Sending rating 0 or 6, a blank comment, or a nonpositive sport ID: 400
  `request.validation_failed`.
- Reviewing a sport on a newly submitted venue before registering its offering:
  400 `venue.sport_not_offered`.
- Supplying latitude 91 or a missing address: 400 `request.validation_failed`.
- Supplying `america/new_york` or `Mars/Olympus`: 400 `venue.invalid_timezone`.
- Omitting the API key: 401. A new client key also creates a new review namespace,
  so it cannot be used to update the original client's review.

No required ordering exists between unrelated reviews or venue creations. The
required dependencies are: obtain a client key before either operation; create
or select a venue before reviewing it; register an offering before reviewing a
specific sport.
