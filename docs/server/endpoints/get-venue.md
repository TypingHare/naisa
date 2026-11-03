# Get venue details

`GET /v1/venues/{venueId}`

Returns a venue's stored details, sports, and weekly opening hours.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

Provide the venue ID as a signed 64-bit integer in the path. No body is needed.

```http
GET /v1/venues/1
```

## Response

HTTP 200:

```json
{
  "message": "Venue retrieved.",
  "data": {
    "venueId": 1,
    "name": "Venue A",
    "address": "123 Example Street, New York, NY",
    "latitude": 40.81,
    "longitude": -73.96,
    "timezone": "America/New_York",
    "sports": [{
      "sportId": 1,
      "name": "Basketball",
      "hourlyPrice": 20.0,
      "isIndoor": true,
      "hasLighting": null
    }],
    "openingIntervals": [{
      "dayOfWeek": "MONDAY",
      "opensAt": "09:00:00",
      "closesAt": "21:00:00"
    }]
  }
}
```

Sports and opening intervals have no guaranteed ordering. Missing sports or
hours produce empty lists. Null lighting means unknown.

Opening hours use the venue's time zone. Weekdays are uppercase names from
`MONDAY` through `SUNDAY`. Closing times at or before opening times fall on the
next day; equal times represent 24 hours. Multiple intervals per day are allowed.
The endpoint returns details even when the venue is closed.

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.invalid_parameter` | Venue ID cannot be parsed as a signed 64-bit integer |
| 404 | `venue.not_found` | Venue does not exist |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
