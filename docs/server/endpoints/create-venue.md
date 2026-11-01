# Create a venue

`POST /v1/venues`

Creates a venue and records the authenticated client as its submitter.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

Send `Content-Type: application/json`. All fields below are required.

| Body field | Rules |
| --- | --- |
| `name` | Nonblank string; at most 255 characters |
| `address` | Nonblank string; at most 255 characters |
| `latitude` | Number from -90 to 90 |
| `longitude` | Number from -180 to 180 |
| `timezone` | Nonblank string; at most 32 characters; recognized Java time zone |

```json
{
  "name": "Riverside Court",
  "address": "1 Riverside Drive, New York, NY",
  "latitude": 40.812345,
  "longitude": -73.961234,
  "timezone": "America/New_York"
}
```

Name, address, and time zone are trimmed after field validation. Time zones
are case-sensitive; `America/New_York` and `UTC` are accepted, while `+02:00`
is rejected. Coordinates are supplied by the caller; the address is not
converted into coordinates.

## Response

HTTP 201:

```json
{"message": "Venue created.", "data": {"id": 123}}
```

Every successful request creates a new venue, even if the name or address
already exists. Sports, prices, and opening hours are not created here.
The venue can be retrieved and reviewed immediately, but sport searches need
its offerings and hours to be added separately.

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.invalid_body` | Missing or unreadable JSON body |
| 400 | `request.validation_failed` | Missing or invalid body fields |
| 400 | `venue.invalid_timezone` | Time zone is not recognized |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
