# AI Usage

## Vincent Lai:

### Oct. 20, 2026:

Used Codex to help me develop addVenue and rateVenue operations.

Check chat history with Codex for the above purpose:
https://chatgpt.com/s/cx_6ad825362e608191a7fe8b562d6cfab5

```text
input_tokens             : 3193066
cached_input_tokens      : 2807680
cache_write_input_tokens : 0
output_tokens            : 28506
reasoning_output_tokens  : 5020
total_tokens             : 3221572

Uncached input: 385386
```

## Andrew Hsu:

### Oct. 23-24, 2026:

**Tool:** Codex  
**Model:** GPT 6.1 Sol  
**Usage:** I used AI to help plan, implement, review, and manually test
operations 1-4.

#### Operation 1

```text
input_tokens            : 34728874
cached_input_tokens     : 34470272
cache_write_input_tokens: 0
output_tokens           : 66203
reasoning_output_tokens : 16036
total_tokens            : 34795077
uncached_input_tokens   : 258602
```

#### Operation 2

```text
input_tokens            : 14463618
cached_input_tokens     : 14043392
cache_write_input_tokens: 0
output_tokens           : 29812
reasoning_output_tokens : 9560
total_tokens            : 14493430
uncached_input_tokens   : 420226
```

#### Operation 3

```text
input_tokens            : 9475630
cached_input_tokens     : 9288064
cache_write_input_tokens: 0
output_tokens           : 29238
reasoning_output_tokens : 6927
total_tokens            : 9504868
uncached_input_tokens   : 187566
```

#### Operation 4

```text
input_tokens            : 13985028
cached_input_tokens     : 13923200
cache_write_input_tokens: 0
output_tokens           : 24669
reasoning_output_tokens : 6165
total_tokens            : 14009697
uncached_input_tokens   : 61828
```

#### Review and feedback cleanup

```text
input_tokens            : 2138751
cached_input_tokens     : 2023552
cache_write_input_tokens: 0
output_tokens           : 6086
reasoning_output_tokens : 1797
total_tokens            : 2144837
uncached_input_tokens   : 115199
```

### Oct. 25-26, 2026:

**Tool:** Codex

**Model:** GPT 6.1 Sol

**Usage:** I used Codex to plan and generate unit tests for operations 1-4.

```text
input_tokens            : 22519378
cached_input_tokens     : 22290688
cache_write_input_tokens: 0
output_tokens           : 57339
reasoning_output_tokens : 15934
total_tokens            : 22576717
uncached_input_tokens   : 228690
```

## Emily Wang:

### Oct. 20-24, 2026:

Used Claude Code (Claude Opus 5.5) and Codex to help me implement operations 5-7
(compareVenues, reportVenueStatus, getVenueStatus) and generate the unit tests
and API tests for them.

```text
input_tokens                : 672
cache_creation_input_tokens : 3725510
cache_read_input_tokens     : 103786625
total_input_tokens          : 107512807
output_tokens               : 340057
total_tokens                : 107852864
```

## Vincent Lai: Operations 8-9 tests and documentation (Oct. 26, 2026)

Tool: OpenAI Codex desktop app, obtained from https://openai.com/codex/.
Codex generated the submission unit tests, Postman collection, supplemental SQL
assertions, and documentation updates, and helped reproduce and verify a database
uniqueness defect. AI-assisted Java and SQL files identify their authors; the
Postman collection identifies AI use in its description. Existing migration V3
was reused from Andrew Hsu's `test/all-endpoint-api-tests` branch with its original
author attribution during development. The final submission branch reuses the
review constraint fix already merged as migration V4 in PR #39; it adds no
duplicate migration. Numeric changes and tests from PR #34 are likewise inherited
from main, with their original attribution preserved.

Prompt:

> Help me add unit tests and API tests for addVenue and rateVenue, and complete
> the documentation for those two operations.

Follow-up prompt:

> For our operations, longtitude and latitude need to be doubles, and rating should
> be smallint. how do we fix this prs

Codex aligned the submission fixtures and API error expectations with Java
`Double` coordinates and `Short` ratings, updated the operation documentation,
and reran the tests and reports. The updated token snapshot below includes this
follow-up and subsequent review/submission work through its stated timestamp.

Submission follow-up prompt:

> check and push our operation PRs, finish style-checking evidence, for document
> team contributions, can you look through the code base, find all the @author
> tags, and for each person, document what they worked on, and add these to a new
> doc on the docs/ folder

Codex reconciled the focused tests/documentation with current main, enabled
Checkstyle on Java test sources, generated verification reports and source
hashes, and compiled `docs/team-contributions.md` from source author tags and
explicit PR/file attribution. The document distinguishes shared credit and AI
tools from human authors. Production Java changes in this PR correct Javadoc;
the existing behavior is exercised by the added tests. No external AI-generated
code was copied without its attribution.

Validation used a disposable PostgreSQL database, real HTTP via Newman, JUnit
Jupiter/Mockito, Checkstyle, and JaCoCo. Reports are in
`server/reports/venue-submissions/`.

Token usage below is the difference between this chat's cumulative usage before
that request and the recorded snapshot at 2026-10-26 22:26:47 America/New_York
(2026-10-27 02:26:47 UTC). It includes the intervening PR reviews, numeric
compatibility fixes, and submission preparation. It replaces the earlier
19:18 snapshot; do not add the two snapshots together. It excludes the preceding
requirements audit and any work after that snapshot; it is not a final session
total. Cached input is a subset of input, and reasoning
output is a subset of output, so neither is added again to the total.

```text
input_tokens            : 14417506
cached_input_tokens     : 14201088
cache_write_input_tokens: 0
output_tokens           : 67139
reasoning_output_tokens : 16586
total_tokens            : 14484645
uncached_input_tokens   : 216418
```

Source: local Codex session token-count events. Update this snapshot from the
completed session before the team's final submission; do not estimate missing
usage or count this snapshot again as a separate session.
