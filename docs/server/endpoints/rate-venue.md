# Rate a venue

`POST /v1/venues/{venueId}/reviews`

Creates or updates a user's review of a venue or one of its sports.

Requires `Authorization: Bearer <apiKey>`. Responses use the
[Capsule format](../architecture.md#capsule).

## Request

Send `Content-Type: application/json`. Provide the venue ID in the path.

| Body field | Rules |
| --- | --- |
| `externalUserID` | Required nonblank string; at most 128 characters; capitalization matters |
| `rating` | Required integer from 1 through 5; decimal JSON numbers such as `4.0` are rejected |
| `comment` | Required nonblank string; at most 2,000 characters |
| `sportId` | Optional positive integer; omit or use null for the whole venue |

```json
{
  "externalUserID": "member-42",
  "rating": 5,
  "comment": "Clean court with plenty of space."
}
```

The venue must offer the specified sport. `externalUserID` belongs to the
calling client's user system; the service does not authenticate that user.
The identifier is preserved exactly, including case and whitespace. Comments
are trimmed after length validation.

## Response

HTTP 200 for both creation and update:

```json
{"message": "Review saved.", "data": {"id": 456}}
```

A review is identified by client, external user ID, venue, and optional sport.
Submitting the same combination updates the rating and comment and returns
the same ID. Its creation time is retained. Whole-venue and sport-specific
reviews are separate, as are reviews submitted through different clients.

## Errors

| Status | `errorCode` | Cause |
| --- | --- | --- |
| 400 | `request.invalid_body` | Missing or unreadable JSON, including decimal or overflowing ratings |
| 400 | `request.validation_failed` | Missing or invalid fields, including integer ratings outside 1–5 |
| 400 | `request.invalid_parameter` | Invalid venue ID format |
| 400 | `venue.sport_not_offered` | Venue does not offer the specified sport |
| 404 | `venue.not_found` | Venue does not exist |

All endpoints return HTTP 401 for a missing header
(`authorization.no_api_key_provided`), an invalid Bearer header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Unexpected failures return HTTP 500 with
`server.internal_error`.
