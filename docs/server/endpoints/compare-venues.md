# Compare venues

`GET /v1/venues/comparison`

Compares two to five venues for a sport. This describes the current
implementation; venue comparison is being refactored.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

All query parameters are required:

| Parameter | Rules |
| --- | --- |
| `venueIds` | 2–5 distinct positive IDs, separated by commas |
| `sportId` | Positive sport ID; every venue must offer it |
| `latitude` | User latitude, -90 to 90 |
| `longitude` | User longitude, -180 to 180 |

```http
GET /v1/venues/comparison?venueIds=1,2&sportId=1&latitude=40.81&longitude=-73.96
```

## Response

HTTP 200:

```json
{
  "message": "Venues compared.",
  "data": {
    "venues": [
      {"venueId": 1, "name": "Venue A", "distanceKm": 0.5, "hourlyPrice": 20.0, "isIndoor": true, "openNow": true, "crowdLevel": "LIGHT"},
      {"venueId": 2, "name": "Venue B", "distanceKm": 1.2, "hourlyPrice": 10.0, "isIndoor": false, "openNow": false, "crowdLevel": null}
    ],
    "nearestVenueId": 1,
    "cheapestVenueId": 2,
    "leastCrowdedVenueId": 1
  }
}
```

Distances are in kilometers and prices are hourly dollar amounts for the sport.
Results are ordered by distance, then venue ID. The three selected IDs identify
the nearest, cheapest, and least crowded venues separately. Ties use the lowest
venue ID; no overall score is calculated.

`openNow` uses each venue's local time and opening hours. It is null when hours
or the time zone are unavailable. Crowd levels use the
[crowd-status calculation](get-crowd-status.md) for the requested sport.
Unknown levels are excluded when choosing the least crowded venue; if all are
unknown, `leastCrowdedVenueId` is null.

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.validation_failed` | Invalid query fields or duplicate venue IDs |
| 400 | `venue.sport_not_offered` | A venue does not offer the sport |
| 404 | `venue.not_found` | A venue does not exist |
| 404 | `sport.not_found` | Sport does not exist |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
