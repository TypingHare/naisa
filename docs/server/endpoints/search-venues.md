# Search venues

`GET /v1/venues`

Finds venues that offer a sport, fit the budget, are close enough to every
member, and are open at the requested time.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

| Query parameter | Rules |
| --- | --- |
| `coordinates[i].latitude` | Required for each member; -90 to 90 |
| `coordinates[i].longitude` | Required for each member; -180 to 180 |
| `sportId` | Required positive integer |
| `date` | Required; `YYYY-MM-DD` |
| `time` | Required; use `HH:mm` |
| `maxDistance` | Required nonnegative distance in kilometers, for each member |
| `budget` | Optional nonnegative hourly budget in dollars; defaults to 0 |

Supply 1–10 coordinates, with consecutive indices starting at 0. Use the same
list format for one person. Date and time are interpreted in `America/New_York`
and must not be in the past.

```http
GET /v1/venues?coordinates[0].latitude=40.81&coordinates[0].longitude=-73.96&sportId=1&date=2027-06-01&time=18:30&maxDistance=2&budget=20
```

URL-encode square brackets when required by the client.

## Response

HTTP 200:

```json
{
  "message": "Venues found.",
  "data": [{
    "venueId": 1,
    "name": "Venue A",
    "memberDistancesKm": [0.5],
    "hourlyPrice": 20.0,
    "isIndoor": true,
    "hasLighting": null
  }]
}
```

Distances follow the request's coordinate order. Price, indoor status, and
lighting apply to the requested sport; null lighting means unknown. Results
have no guaranteed ordering. No matches returns `data: []` with the message
`No venues match the search parameters.`

## Behavior

- Every member must be within `maxDistance`; distance is measured directly
  between coordinates, not along roads.
- The sport's hourly price must not exceed `budget`; a zero budget selects
  free offerings.
- Opening time is included; closing time is excluded. An earlier closing time
  means the interval ends the next day. Equal times represent 24 hours.
- Venues without opening hours are excluded. Crowd levels, bookings, and the
  duration of an activity are not checked.
- Alternatives are available through a [separate endpoint](search-alternatives.md).

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.validation_failed` | Missing or invalid query fields, or a past date and time |
| 404 | `sport.not_found` | Sport does not exist |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
