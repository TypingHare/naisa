# Get crowd status

`GET /v1/venues/{venueId}/crowd-status`

Estimates the current crowd level from recent reports.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

Provide the venue ID in the path. An optional `sportId` query parameter selects
a sport that the venue offers.

```http
GET /v1/venues/1/crowd-status?sportId=1
```

Without `sportId`, only venue-wide reports are considered. With `sportId`, both
venue-wide reports and reports for that sport are considered.

## Response

HTTP 200:

```json
{
  "message": "Crowd status retrieved.",
  "data": {
    "venueId": 1,
    "source": "LIVE",
    "reportCount": 3,
    "latestReportAt": "2027-06-01T18:30:00Z",
    "crowdLevelScore": 2.8,
    "crowdLevel": "BUSY"
  }
}
```

With no eligible reports, `source` is `UNKNOWN`, `reportCount` is 0, and
`latestReportAt`, `crowdLevelScore`, and `crowdLevel` are null.

## Calculation

1. Consider reports from the past 120 minutes.
2. Keep each reporter's latest eligible report. Reporters are identified by
   client and external user ID. Equal timestamps are resolved by the higher
   report ID.
3. Map `EMPTY`, `LIGHT`, `MODERATE`, `BUSY`, and `PACKED` to scores 0–4.
4. Weight reports by age in whole minutes: `2 ^ (-age / 60)`. Multiply the
   weight by 0.5 for venue-wide reports and 1.0 for sport-specific reports.
5. Divide the sum of weighted scores by the sum of weights. Round to the
   nearest integer to choose the crowd-level name; halfway values round up.

For example, equally recent venue-wide EMPTY and sport-specific BUSY reports
from different reporters give `(0.5 * 0 + 1 * 3) / (0.5 + 1) = 2`, or MODERATE.
`reportCount` and `latestReportAt` refer to the selected reports.

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.invalid_parameter` | Invalid venue or sport ID format |
| 400 | `venue.sport_not_offered` | Venue does not offer the specified sport |
| 404 | `venue.not_found` | Venue does not exist |
| 500 | `crowd.invalid_crowd_level_string` | A selected report contains an invalid level |
| 500 | `crowd.invalid_crowd_level_score` | The calculated score is outside 0–4 |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
