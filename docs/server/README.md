## Server

All documents in this directory discuss the `server` workspace, so all file
paths are relative to `server`. When developing the backend (server-side), we
open `server` as the project root directory in the IDE so that the IDE can find
`pom.xml` and recognize it as a Java project.

In [architecture.md](./architecture.md), we discuss the architecture of the
backend, which is a typical Spring Boot project. An application's architecture
refers to the major design decisions regarding dependencies (e.g., third-party
libraries and CLI tools), components (e.g., layers and utilities), communication
(e.g., protocols between endpoints), and so on.

In [venue-apis.md](./venue-apis.md), we describe the API contracts for all seven proposed operations.


## Additional endpoints

All JSON responses use the `Capsule` format described in the architecture guide.
Successful responses include `message` and, where applicable, `data`. Errors
include `message` and `errorCode`. The health response has no body.

### Health and client registration

`GET /v1/health` requires no key or input. It returns HTTP 204 with no body and
makes no database changes.

`POST /v1/clients` requires no key or request body. It creates a new client on
every call and returns HTTP 201 with `data.id` and `data.apiKey`. Use that key
in `Authorization: Bearer <apiKey>` for all venue endpoints. Do not register a
new client for each request if the requests should share one client identity.

### Create a venue

`POST /v1/venues` requires an API key and a JSON body. For example:

```json
{
  "name": "Riverside Court",
  "address": "123 Example Street",
  "latitude": 40.81,
  "longitude": -73.96,
  "timezone": "America/New_York"
}
```

Name and address are required, nonblank strings of at most 255 characters.
Latitude and longitude are required finite numbers in [-90, 90] and [-180, 180].
Timezone is a required, nonblank string of at most 32 characters and must name a
recognized region. Surrounding whitespace is removed from name, address, and
timezone. Each call inserts a new venue attributed to the authenticated client;
matching names or addresses do not prevent duplicates. It does not create sport
offerings, prices, or opening intervals.

HTTP 201 returns `data: {"id": 123}` with the new venue ID. Read it back through
`GET /v1/venues/123`. An unrecognized timezone returns HTTP 400 with
`venue.invalid_timezone`. Missing or invalid fields return HTTP 400 with
`request.validation_failed`; malformed JSON returns `request.invalid_body`.
For example, an empty name or latitude of 100 is invalid.

### Create or update a review

`POST /v1/venues/{venueId}/reviews` requires an API key, an existing venue, and a
JSON body. For example:

```json
{
  "externalUserID": "user-1",
  "sportId": 1,
  "rating": 4,
  "comment": "Good court."
}
```

The key is spelled `externalUserID`, with uppercase `ID`. It is required,
nonblank, at most 128 characters, and scoped to the authenticated client.
`sportId` is optional; omit it or use null to review the whole venue. Otherwise,
it must be a positive integer identifying a sport offered at that venue.
`rating` is a required integer JSON token from 1 through 5. `comment` is required,
nonblank, and at most 2000 characters. Surrounding comment whitespace is removed.

The request inserts a review, or updates the rating and comment for the same
client, user, venue, and sport. An update retains the review ID and creation
time. Other users, clients, and sports have independent reviews. Both insert
and update return HTTP 200 with `data: {"id": 123}`.

An unknown venue returns HTTP 404 with `venue.not_found`. An unsupported sport
returns HTTP 400 with `venue.sport_not_offered`. Invalid fields return HTTP 400
with `request.validation_failed`. Decimal ratings such as 4.5 or 4.0, malformed
JSON, and integer overflow return HTTP 400 with `request.invalid_body`.
A rating of 0 or an empty comment is an example of incorrect use.

### Shared errors

Protected endpoints return HTTP 401 for a missing key
(`authorization.no_api_key_provided`), a malformed authorization header
(`authorization.invalid_authorization_header`), or an unknown key
(`client.invalid_api_key`). Invalid path parameter formats return HTTP 400 with
`request.invalid_parameter`. Unexpected server failures return HTTP 500 with
`server.internal_error`.
