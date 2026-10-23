### Venue APIs

This document describes the API contracts for Operations 5, 6, and 7. All
endpoints require authorization and use the standard `Capsule` response format.
See [architecture.md](./architecture.md) for details.

#### Operation 5: Compare Venues

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
        "best": {
            "nearest": 1,
            "cheapest": 2,
            "leastCrowded": 1
        }
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

The venues are sorted by distance, nearest first. The `best` object contains the
venue IDs with the shortest distance, lowest price, and lowest crowd level. Ties
are broken by the lowest venue ID. If no recent crowd reports are available,
`crowdLevel` and `best.leastCrowded` are `null`.

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

#### Operation 6: Report Venue Status

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

#### Operation 7: Get Venue Status

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
