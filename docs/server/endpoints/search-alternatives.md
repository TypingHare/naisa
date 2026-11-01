# Search alternative venues

`GET /v1/venues/alternatives`

Suggests venues and search values using only changes allowed by the client.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

Use the parameters from [search venues](search-venues.md), including the
`coordinates` list. Also supply `allowedChanges`: a nonempty, comma-separated
list containing any of `DATE`, `TIME`, `MAX_DISTANCE`, and `BUDGET`.
Names are case-sensitive. The original date and time must not be in the past.

```http
GET /v1/venues/alternatives?coordinates[0].latitude=40.81&coordinates[0].longitude=-73.96&sportId=1&date=2027-06-01&time=18:30&maxDistance=2&budget=10&allowedChanges=TIME,MAX_DISTANCE
```

## Response

HTTP 200:

```json
{
  "message": "Alternatives found.",
  "data": [{
    "venueSearchResult": {
      "venueId": 1,
      "name": "Venue A",
      "memberDistancesKm": [3.5],
      "hourlyPrice": 8.0,
      "isIndoor": true,
      "hasLighting": true
    },
    "date": "2027-06-01",
    "time": "19:30:00",
    "maxDistance": 3.5,
    "averageDistance": 3.5,
    "budget": 10.0,
    "numChangedConstraint": 2,
    "dateTimeDifferenceInMinutes": 60
  }]
}
```

`averageDistance` is the mean member distance in kilometers.
`numChangedConstraint` counts actual changes to date, time, distance, and budget.
`dateTimeDifferenceInMinutes` is the absolute difference from the requested time.

## Behavior

Permitted changes are limited to:

- `DATE`: up to 3 days before or after the requested date.
- `TIME`: offsets of -2, -1, 0, +1, or +2 hours.
- `MAX_DISTANCE`: up to 5 extra kilometers for each member.
- `BUDGET`: up to 20 extra dollars per hour.

Suggestions must not be in the past or outside the permitted date range.
The venue must be open. Distance and budget stay unchanged unless an increase
is needed; the suggested increase is only enough to include that venue.
An unchanged match is allowed. A venue can appear at several suggested times.

Return at most 10 entries, ordered by actual change count, absolute time
difference, average member distance, then venue ID, all ascending.

If no offering fits both the permitted budget and distance, return HTTP 404.
If offerings fit those limits but none are open at a suggested time, return
HTTP 200 with `data: []` and `No alternatives match the permitted changes.`

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.validation_failed` | Invalid search fields or allowed changes, or a past original time |
| 404 | `sport.not_found` | Sport does not exist |
| 404 | `venue.no_nearby_venues` | No offering fits the permitted budget and distance |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
