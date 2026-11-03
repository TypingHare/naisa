# T1: Project Proposal

## Part 1: Team

Nothing has changed.

## Part 2: Service Functionality

### Operation 1: Find Venues

**Inputs**

- `location: Location` - origin location for the search
- `sport: Sport` - sport to search
- `date: LocalDate` - date to search
- `time: LocalTime` - time to search
- `maxDistance: double` - radius distance from location to search within
- `budget: double` - budget for venues that require payment (0 default)

**Computation**

- Converts the location into geographic coordinates.
- Filters stored venues by supported sport.
- Computes the distance between the requested location and each candidate venue.
- Filters venues by distance, operating hours, price, and availability.

**Returns**

- a list of valid venue results, each containing the following entry results:
    - venue identifier
    - venue name
    - distance
    - estimated price
    - relevant facility information
    - availability information
- if no valid venues exist, calls `findAlternatives`

### Operation 2: Find Alternatives

**Inputs**

- `location: Location` - origin location for the search
- `sport: Sport` - sport to search
- `date: LocalDate` - date to search
- `time: LocalTime` - time to search
- `maxDistance: double` - radius distance from location to search within
- `budget: double` - budget for venues that require payment (0 default)
- `dateAvailable: Boolean` - set **False** if `date` in `findVenues()` did not
  work out
- `timeAvailable: Boolean` - set **False** if `time` in `findVenues()` did not
  work out
- `maxDistanceAvailable: Boolean` - set **False** if no venues within
  `maxDistance` in `findVenues()`
- `budgetAvailable: Boolean` - set **False** if no venue is cheaper than
  `budget` in `findVenues()`

**Computation**

- Suggests new values for each variable with their corresponding boolean
  variable set to **False**.
    - For example, when the client calls `findVenues`, but there is no venue
      available on the `date` it requested, `findAlternatives` will receive
      `dateAvailable` = **False**, so it will suggest venues available on an
      alternative date, such as the following day.
- Calls `findVenues()` with the suggested parameters.

**Returns**

- a list of venue results, each containing the following entry results:
    - venue identifier
    - venue name
    - distance
    - estimated price
    - relevant facility information
    - availability information
- if no venue for the sport is nearby, return an error message "There are no
  venues for this sport nearby"

### Operation 3: Find Group Venues

**Inputs**

- `locations: List<Location>` - locations of participating group members
- `sport: Sport` - sport to search
- `date: LocalDate` - date to search
- `time: LocalTime` - time to search
- `distanceFromCenter: double` - radius distance from the center of all group
  members' locations to search within (optional)
- `budget: double` - budget for venues that require payment (0 default)

**Computation**

- Converts the locations into geographic coordinates.
- Filters stored venues by supported sport.
- Computes the average distance between all members and each candidate venue and
  distance between each member and each venue.
- Filters venues by distance, operating hours, price, and availability.

**Returns**

- a list of venue results, each containing the following entry results:
    - venue identifier
    - venue name
    - average distance for all members
    - distance from each member
    - estimated price
    - relevant facility information
    - availability information

### Operation 4: Get Venue Info

**Inputs**

- `venueId: String` - identifier of the queried venue
- `venueName: String` - name of the queried venue

**Computation**

- Look up the venue identifier in the service's venue datastore and rejects the
  request if more than one identifiers is given or the identifier is unknown.

**Returns**

- a list of the queried venue's information, including sport types, hours, field
  types, location, etc.

### Operation 5: Compare Venues

**Inputs**

- `venueIds: List<String>` - identifiers of the venues to compare (2 to 5)
- `sport: Sport` - sport the comparison is evaluated for
- `location: Location` - (optional) origin location used to compute distance
- `date: LocalDate` - (optional) date to evaluate, defaults to the current date
- `time: LocalTime` - (optional) time to evaluate, defaults to the current time

**Computation**

- Looks up each venue identifier in the service's venue datastore and rejects
  the request if fewer than two identifiers is given or any identifier is
  unknown.
- Evaluates every venue on the same set of dimensions for the requested sport,
  date, and time: distance from the origin location, estimated price, whether
  the venue is open and available, facility attributes (e.g., surface type,
  lighting), and the current crowd level derived from stored status reports (the
  same aggregation as Operation 7).
- Normalizes each dimension to a common scale, determines which venue is best on
  each dimension, and combines the dimensions into an overall score used to rank
  the venues.

**Returns**

- a list of comparison entries ordered by overall rank, each containing:
    - venue identifier and venue name
    - the value for each dimension (distance, estimated price, availability,
      facility attributes, crowd level)
    - overall score and rank
- for each dimension, the identifier of the venue that is best on it
- an error response naming the invalid identifiers if the request is rejected

### Operation 6: Report Venue Status

**Inputs**

- `venueId: String` - identifier of the venue the report is about
- `crowdLevel: CrowdLevel` - one of `EMPTY`, `LIGHT`, `MODERATE`, `BUSY`,
  `PACKED`
- `sport: Sport` - (optional) the facility within the venue the report applies
  to; applies to the whole venue if omitted
- `observedAt: LocalDateTime` - (optional) when the observation was made,
  defaults to the time the request is received
- `reporterId: String` - opaque identifier assigned by the calling client for
  the source of the report

**Computation**

- Validates that the venue exists in the venue datastore, that the crowd level
  is one of the allowed values, and that `observedAt` is neither in the future
  nor older than the freshness window (2 hours).
- Checks stored reports for an earlier report with the same `reporterId`, venue,
  and sport inside the freshness window; if one exists, replaces it instead of
  adding a second one, so a single source cannot dominate the aggregate.
- Writes the report, with its timestamp, to the service's status report
  datastore.
- Recomputes the venue's aggregated crowd level from all stored reports that are
  still inside the freshness window.

**Returns**

- report identifier
- recorded timestamp
- whether the report was newly created or replaced an earlier one
- the venue's updated aggregated crowd level and the number of reports it is
  based on
- an error response describing the failed validation if the report is rejected

### Operation 7: Get Venue Status

**Inputs**

- `venueId: String` - identifier of the venue
- `sport: Sport` - (optional) restricts the status to one facility within the
  venue

**Computation**

- Reads the reports stored for the venue (and sport, if given) that fall inside
  the freshness window (2 hours).
- Weights each report by its age, so that recent reports count more than older
  ones, and computes a single aggregated crowd level from the weighted reports.
- Derives a confidence value from the number of reports and how recent they are.
- If no reports fall inside the freshness window, computes a typical crowd level
  from the stored historical reports for the same day of week and hour, and
  marks the result as an estimate rather than a live status.

**Returns**

- venue identifier
- aggregated crowd level
- confidence value
- number of reports used
- timestamp of the most recent report
- status source: `LIVE` (from recent reports), `HISTORICAL` (estimated from past
  reports), or `UNKNOWN` (no stored reports for the venue)

## Part 3: Client Programs

### Fitness Platforms

Fitness tracking platforms such as Strava or Nike Run Club are primarily
designed to record workouts, analyze fitness activity, and help users plan
training. These programs could call our service when they need to identify
sports venues appropriate for a planned activity. For example, a running
application could request nearby venues that contain a running track and satisfy
constraints such as distance, operating hours, or lighting.

### Sports Pickup Organizer Platforms

Pickup and recreational sports platforms such as Volo or GoodRec are designed to
organize games, leagues, and other group activities. These programs could call
our service to identify suitable practice or meetup venues for a sport,
particularly outside of their own scheduled events. A client could provide the
sport, participant locations, desired time, and budget, and use the returned
venue options when coordinating a group activity.

### NYC Department of Parks & Recreation

An NYC Parks and Recreation website serves a broader informational purpose,
including helping residents learn about parks, facilities, programs, and public
recreation resources. The website could call our service to provide more
specialized sports-facility discovery than its general park search. For example,
it could request venues that support a particular sport and satisfy location,
time, or facility constraints, then incorporate those results into its existing
recreation information.

## Part 4: Development Tools

Below are the tools we are going to use in this project (excluding GitHub, which
is mandatory):

1. Build tool & dependency manager: **Maven**. Some of our teammates have
   limited experience developing Java web applications. Although **Gradle** is
   more modern and used more frequently nowadays, it requires developers to
   learn how to write Gradle scripts (`build.gradle`) or Kotlin scripts
   (`build.gradle.kts`). Also, Maven is used widely in many legacy Java projects
   (many Java projects are kinda legacy) and still has a large market share.
2. Test runner: **JUnit 5**. This is the standard testing framework for a Spring
   Boot project.
3. Tools for testing API endpoints: **Postman** and/or **cURL**. **Postman** is
   a mature tool for testing and managing API endpoints, while **cURL** is a
   simple command-line tool for sending HTTP requests. Team members can choose
   whichever they prefer.
4. Mocking framework: **Mockito**. It is the primary and default mocking
   framework in a Spring project.
5. Test coverage tracker: **JaCoCo**. It is a powerful test coverage tool that
   shows which lines are not covered by unit tests.
6. Linters: **Checkstyle** and **PMD**. People now often refer to static code
   analysis tools as linters. A linter should normally identify potential bugs
   and check code style, but neither **Checkstyle** nor **PMD** can do both, and
   there are no other suitable alternatives. Therefore, we use both tools for
   linting.
7. Formatter: **google-java-format**. We want our code to comply with the Google
   Java Style Guide, and **google-java-format** can save us the time we would
   otherwise spend formatting files manually. It parses the code into an AST and
   formats it based on the rules defined in the Google Java Style Guide.
8. IDE/editor: **IntelliJ IDEA** / **Visual Studio Code** / **Neovim**. Team
   members can choose the IDE or editor that works best for them.
9. Debugging: **IntelliJ IDEA** / **GenAI**. IntelliJ IDEA comes with a powerful
   debugger that makes the tedious work of debugging easier. Sometimes GenAI can
   find bugs much faster than humans, but it may identify a superficial problem
   instead of the underlying one. We think combining a debugger with GenAI works
   best for us.
10. Continuous integration: **GitHub Actions**. It is a popular and useful tool
    that is worth learning.
11. Project management: **GitHub Projects**. Our team has only 4 members, so it
    is pretty small. Professional project management tools seem like overkill,
    and we may spend too much time learning and using them. Since we already use
    GitHub to host our remote repository, we will use GitHub Projects to manage
    this project.
12. Database: **PostgreSQL**. It is a free and open-source relational database
    that receives a lot of praise from the community.
13. OpenAPI documentation: **Swagger**. It is a suite of open-source and
    enterprise tools used to document RESTful APIs. There is a Java library
    called `springdoc-openapi-starter-webmvc-ui` that helps integrate Swagger
    into a Java project.
