### Venue APIs

This document describes the API contracts for Operations 1, 2, 3, 5, 6, and 7. All
endpoints require authorization and use the standard `Capsule` response format.
See [architecture.md](./architecture.md) for details.

### Operations 1 and 3: Find Venues

`GET /v1/venues`

This endpoint finds open venues for one person or a group. Both searches use
query parameters and return the same response format.

#### Request

The following query parameters are required:

- `sportId`: The ID of the requested sport
- `date`: The desired date in YYYY-MM-DD format, for example 2026-10-23.
- `time`: The desired time on a 24-hour clock. Accept HH:mm, HH:mm:ss, or
  HH:mm:ss with 1–9 fractional second digits. Examples: 14:30, 14:30:45, and
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
  into the next day. For example, Monday 22:00–02:00 includes Tuesday 01:00.
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

### Operation 5: Compare Venues

`GET /v1/venues/comparison`

This endpoint compares 2–5 venues for a given sport. Venues are sorted by
distance from the user's location.

##### Request

The following query parameters are required:

- `venueIds`: 2–5 distinct venue IDs, separated by commas.
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
