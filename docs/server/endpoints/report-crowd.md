# Report crowd level

`POST /v1/venues/{venueId}/crowd-reports`

Creates a new crowd report for a venue or one of its sports.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

Send `Content-Type: application/json`. Provide the venue ID in the path.

| Body field | Rules |
| --- | --- |
| `externalUserId` | Required nonblank string; at most 128 characters |
| `crowdLevel` | Required; `EMPTY`, `LIGHT`, `MODERATE`, `BUSY`, or `PACKED` |
| `sportId` | Optional positive integer; omit or use null for the whole venue |

```json
{
  "externalUserId": "member-42",
  "crowdLevel": "BUSY",
  "sportId": 1
}
```

The venue must offer the specified sport. Each submission creates a separate
report with a database-generated timestamp; earlier reports remain stored.
A reporter is identified by the authenticated client and `externalUserId`.
The [crowd-status endpoint](get-crowd-status.md) selects each reporter's latest
eligible report.

## Response

HTTP 201:

```json
{"message": "Crowd report created.", "data": {"id": 123}}
```

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.invalid_body` | Missing or unreadable JSON body |
| 400 | `request.validation_failed` | Missing or invalid body fields |
| 400 | `request.invalid_parameter` | Invalid venue ID format |
| 400 | `venue.sport_not_offered` | Venue does not offer the specified sport |
| 404 | `venue.not_found` | Venue does not exist |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
