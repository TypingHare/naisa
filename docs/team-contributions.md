# Team contributions - Iteration 1

This retrospective records work attributed by `@author` tags in the Java source
and tests. It covers main at `11c68c4` plus the operations 8-9 tests in this PR.
Vincent Lai used Codex to compile this document on October 26, 2026.

Tags establish recorded contribution, not exclusive ownership, hours worked, or
the exact size of a person's change. A class-level tag credits the class; a
method-level tag gives more specific evidence. Shared tags retain all named
contributors. Codex and Claude Code are AI tools, not team members. The spelling
`James Chen.` is normalized to James Chen in the summary but retained below.

## James Chen

- Built shared HTTP infrastructure: the response capsule, authorization
  annotation/interceptor, request logging, web configuration, common exceptions,
  and global exception handling.
- Implemented client registration/API-key issuance and lookup through
  `ClientController`, `ClientService`, and `ClientRepository`; authored the
  health controller and the `Beans` construction utility.
- Co-authored the persistence entities with Vincent Lai: venues, venue sports,
  reviews, crowd reports, sports, and opening intervals. The client entity is
  attributed to James.
- Contributed to `CrowdReportService.reportCrowd` with Emily Wang and the
  venue/sport lookup helper in `VenueService`.

## Vincent Lai

- Implemented operations 8 and 9: venue creation and rating/review upserts,
  including controller methods, submission DTOs, resource-ID responses,
  review/venue repositories, validation errors, and submission attribution.
- Co-authored persistence entities with James Chen and authorization handling
  for authenticated requests. Added request-parameter conversion error handling.
- Contributed numeric-type changes across search, comparison, venue information,
  and rating models/services; shared tags with Andrew Hsu and Emily Wang identify
  the affected components. `NumericRequestTest` verifies their HTTP behavior.
- Added venue-sport lighting persistence support and its persistence test.
- Added `VenueSubmissionServiceTest`: 14 cases covering venue normalization and
  timezone rejection, review creation/update, independent scopes, validation of
  referenced resources, and repository interaction behavior using Mockito.
- Shared credit in the comparison test fixture records the coordinate update;
  it does not imply sole authorship of the comparison suite.

## Andrew Hsu

- Implemented operations 1-4: individual/group venue searches, permitted-change
  alternatives, and venue details, with request/response models, controller
  methods, service logic, and repository queries.
- Added search validation, group-member handling, future date/time suggestions,
  change counting, and opening-hours checks. Shared service/model tags record
  subsequent numeric-type contributions by Vincent Lai.
- Added `VenueSearchTest`, `GroupVenueSearchTest`, `VenueAlternativesTest`, and
  `VenueServiceTest`, covering search behavior and venue-information responses.
- Contributed distance calculation with Emily Wang and the comparison service's
  opening-hours handling, including the 24-hour interval regression test.

## Emily Wang

- Implemented operations 5-7: venue comparison, crowd-report submission, and
  current crowd-status calculation, including controller methods and DTOs/VOs.
- Authored the crowd-report and comparison services, crowd-report repository,
  crowd constants, and shared repository/distance utilities. Tags credit James
  Chen on crowd submission and Andrew Hsu on distance/opening-hours work.
- Added `CrowdReportServiceTest` and the comparison service test suite, including
  repository/clock test doubles, validation/error paths, crowd selection,
  distance/price comparisons, and opening-status behavior. Later comparison
  fixes and fixtures carry Andrew's and Vincent's additional credit.

## Evidence outside Java author tags

The inventory below intentionally does not infer ownership of untagged files.
Additional explicit author comments and merged PRs document work that Java tags
do not capture:

| Contributor | Additional recorded work | Evidence |
| --- | --- | --- |
| Andrew Hsu, Codex | Full HTTP API suite, isolated runner, SQL assertions, review-uniqueness migration V4, and coverage reports | [PR #39](https://github.com/TypingHare/naisa/pull/39), [API suite](../server/api-tests/README.md) |
| Vincent Lai, Codex | Focused operations 8-9 Postman collection, persistence assertions, API documentation, and style evidence in this change | [Collection](../server/api-tests/naisa-venue-submissions.postman_collection.json), [SQL assertions](../server/api-tests/submission-assertions.sql), [API documentation](server/venue-apis.md#operation-8-add-venue), [reports](../server/reports/venue-submissions/summary.txt) |
| Vincent Lai, Codex | Lighting and numeric-type changes | [PR #33](https://github.com/TypingHare/naisa/pull/33), [PR #34](https://github.com/TypingHare/naisa/pull/34) |
| Andrew Hsu, Codex | Venue information, individual/group search, and alternative-search unit suites | [PR #35](https://github.com/TypingHare/naisa/pull/35), [PR #36](https://github.com/TypingHare/naisa/pull/36), [PR #37](https://github.com/TypingHare/naisa/pull/37), [PR #38](https://github.com/TypingHare/naisa/pull/38) |

See [AI usage](AI-Usage.md) for tool disclosures. This source-based retrospective
is now backed by 13 assigned, completed [GitHub task issues](../README.md#project-management).
Those records were created after implementation and link to source and merged-PR
evidence. They do not imply that issues existed during development. Mentor and
Instructor access still needs verification. Review activity, planning, untagged
documentation, and task completion dates must be recorded from their own evidence
rather than inferred from author tags.

## Complete Java author-tag inventory

The following inventory includes every `@author` tag in `server/src/main/java`
and `server/src/test/java`, excluding generated reports, dependency code, and
example snippets in documentation. Line links refer to the source version in
this PR. The associated Javadoc sentence identifies the scope; consult the
linked declaration to distinguish a class from a method. Refresh this inventory
after later source changes; it is not a contribution-percentage calculation.

Scanned 184 author tags across 58 Java files.

### AuthorizationRequired.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L13](../server/src/main/java/com/naisa/common/annotation/AuthorizationRequired.java#L13) | James Chen | Indicates that a controller method requires authorization. |

### Capsule.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L18](../server/src/main/java/com/naisa/common/Capsule.java#L18) | James Chen | Represents a capsule, which defines the structure of the JSON object in the HTTP response body. |

### VenueConstant.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L6](../server/src/main/java/com/naisa/common/VenueConstant.java#L6) | Emily Wang | Defines constants related to venues. |

### VenueDistance.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L6](../server/src/main/java/com/naisa/common/VenueDistance.java#L6) | Emily Wang, Codex, Andrew Hsu, Codex | Calculates direct geographic distances for venue searches and comparisons. |

### WebConfiguration.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L15](../server/src/main/java/com/naisa/config/WebConfiguration.java#L15) | James Chen | Represents general web configuration. |

### GlobalExceptionHandler.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L25](../server/src/main/java/com/naisa/controller/GlobalExceptionHandler.java#L25) | James Chen | This global exception handler handles exceptions caught during request handling. |
| [L101](../server/src/main/java/com/naisa/controller/GlobalExceptionHandler.java#L101) | Vincent Lai, Codex | Reports request parameters that cannot be converted to their declared Java types. |

### ClientController.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L31](../server/src/main/java/com/naisa/controller/v1/ClientController.java#L31) | James Chen | Creates a client by issuing an API key. |

### HealthController.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/controller/v1/HealthController.java#L16) | James Chen | Represents the health controller. |

### VenueController.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L45](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L45) | Vincent Lai, Codex, Andrew Hsu, Codex | Exposes venue-related HTTP endpoints. |
| [L60](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L60) | Vincent Lai, Codex, Emily Wang, Codex, Claude Code, Andrew Hsu, Codex | Creates the controller with its services. |
| [L77](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L77) | Andrew Hsu, Codex | Returns a venue's location, offered sports, and weekly opening intervals. |
| [L96](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L96) | Andrew Hsu, Codex | Finds open venues within the requested distance and hourly budget for one origin or a group. |
| [L115](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L115) | Andrew Hsu, Codex | Finds venue alternatives using only the changes permitted by the client. |
| [L138](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L138) | Vincent Lai, Codex | Accepts basic venue information and creates a venue attributed to its submitter. |
| [L161](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L161) | Vincent Lai, Codex | Accepts a rating and comment for a venue or an optional sport offered there. |
| [L183](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L183) | Emily Wang, Codex | Accepts a crowd report for a venue or an optional sport. |
| [L203](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L203) | Emily Wang, Claude Code | Returns the current crowd status of a venue or one of its sports. |
| [L224](../server/src/main/java/com/naisa/controller/v1/VenueController.java#L224) | Emily Wang, Codex | Compares 2 to 5 venues for a sport based on the user's location. |

### ClientException.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L8](../server/src/main/java/com/naisa/exception/ClientException.java#L8) | James Chen | Represents a client exception. |

### ServiceException.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/exception/ServiceException.java#L16) | James Chen | Represents a service exception. |

### VenueException.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L11](../server/src/main/java/com/naisa/exception/VenueException.java#L11) | Vincent Lai, Codex, Emily Wang, Codex, Andrew Hsu, Codex | Describes business-rule failures encountered when searching, creating, or reviewing venues. |
| [L70](../server/src/main/java/com/naisa/exception/VenueException.java#L70) | Vincent Lai, Codex | Creates an error description with its HTTP status and optional underlying cause. |
| [L84](../server/src/main/java/com/naisa/exception/VenueException.java#L84) | Vincent Lai, Codex | Creates a fresh throwable with the same error description. |
| [L96](../server/src/main/java/com/naisa/exception/VenueException.java#L96) | Vincent Lai, Codex | Creates a fresh throwable that retains the supplied underlying failure. |

### AuthorizationInterceptor.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L33](../server/src/main/java/com/naisa/interceptor/AuthorizationInterceptor.java#L33) | James Chen | This interceptor verifies user authorization when required. |
| [L66](../server/src/main/java/com/naisa/interceptor/AuthorizationInterceptor.java#L66) | James Chen, Vincent Lai, Codex | Authenticates requests whose handler requires a client API key. |

### LoggingInterceptor.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L40](../server/src/main/java/com/naisa/interceptor/LoggingInterceptor.java#L40) | James Chen | This interceptor logs each request before and after it is handled. |

### AddVenueRequest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L21](../server/src/main/java/com/naisa/model/dto/AddVenueRequest.java#L21) | Vincent Lai, Codex | Carries the information required to submit a venue. |

### FindAlternativesRequest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/model/dto/FindAlternativesRequest.java#L16) | Andrew Hsu, Codex | Carries the original search parameters and the changes permitted by the client. |

### FindVenuesRequest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L33](../server/src/main/java/com/naisa/model/dto/FindVenuesRequest.java#L33) | Andrew Hsu, Codex, Vincent Lai, Codex | Carries the query parameters for a venue search. |

### GroupMemberLocation.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L13](../server/src/main/java/com/naisa/model/dto/GroupMemberLocation.java#L13) | Andrew Hsu, Codex, Vincent Lai, Codex | Carries the coordinates of one member in a group venue search. |

### RateVenueRequest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L24](../server/src/main/java/com/naisa/model/dto/RateVenueRequest.java#L24) | Vincent Lai, Codex | Carries a user's rating and written feedback about a venue or one of its sports. |

### ReportCrowdRequest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/model/dto/ReportCrowdRequest.java#L16) | Emily Wang, Codex | Represents a request to report the crowd level at a venue. |

### SearchConstraint.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L6](../server/src/main/java/com/naisa/model/dto/SearchConstraint.java#L6) | Andrew Hsu, Codex | Identifies a search value that the client permits the service to change. |

### VenueComparisonRequest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L18](../server/src/main/java/com/naisa/model/dto/VenueComparisonRequest.java#L18) | Emily Wang, Codex, Vincent Lai, Codex | Request parameters for comparing venues that offer the same sport. |

### Client.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L25](../server/src/main/java/com/naisa/model/entity/Client.java#L25) | James Chen | Represents a client entity. |

### CrowdReport.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L29](../server/src/main/java/com/naisa/model/entity/CrowdReport.java#L29) | James Chen, Vincent Lai | Represents a crowd report entity. |

### OpeningInterval.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L26](../server/src/main/java/com/naisa/model/entity/OpeningInterval.java#L26) | James Chen, Vincent Lai | Represents an opening interval entity. |

### Review.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L33](../server/src/main/java/com/naisa/model/entity/Review.java#L33) | James Chen, Vincent Lai | Represents a review entity. |

### Sport.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/model/entity/Sport.java#L16) | James Chen, Vincent Lai | Represents a sport entity. |

### Venue.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L34](../server/src/main/java/com/naisa/model/entity/Venue.java#L34) | James Chen, Vincent Lai, Codex | Represents a venue entity. |

### VenueSport.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L32](../server/src/main/java/com/naisa/model/entity/VenueSport.java#L32) | James Chen, Vincent Lai, Codex | Represents a venue sport entity. |

### CrowdStatusResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L14](../server/src/main/java/com/naisa/model/vo/CrowdStatusResponse.java#L14) | Emily Wang, Claude Code | Represents the current crowd status of a venue. |

### FindAlternativeResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/model/vo/FindAlternativeResponse.java#L16) | Andrew Hsu, Codex | Describes a venue and the suggested search values that make it a valid alternative. |
| [L36](../server/src/main/java/com/naisa/model/vo/FindAlternativeResponse.java#L36) | Andrew Hsu, Codex | Creates one alternative with its suggested search values. |

### FindVenueResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L17](../server/src/main/java/com/naisa/model/vo/FindVenueResponse.java#L17) | Andrew Hsu, Codex | Describes one venue returned by a venue search. |
| [L43](../server/src/main/java/com/naisa/model/vo/FindVenueResponse.java#L43) | Andrew Hsu, Codex | Creates a response for one venue and a single search origin. |

### ReportCrowdResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L11](../server/src/main/java/com/naisa/model/vo/ReportCrowdResponse.java#L11) | Emily Wang | Represents the result of reporting a venue's crowd level. |

### ResourceIdResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L14](../server/src/main/java/com/naisa/model/vo/ResourceIdResponse.java#L14) | Vincent Lai, Codex | Identifies a resource successfully saved by the service. |
| [L26](../server/src/main/java/com/naisa/model/vo/ResourceIdResponse.java#L26) | Vincent Lai, Codex | Creates a response containing the saved resource's identifier. |

### VenueComparisonItem.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L14](../server/src/main/java/com/naisa/model/vo/VenueComparisonItem.java#L14) | Emily Wang, Codex | Represents the comparison details of a single venue. |

### VenueComparisonResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L15](../server/src/main/java/com/naisa/model/vo/VenueComparisonResponse.java#L15) | Emily Wang, Codex | Represents the result of comparing venues. |

### VenueInfoResponse.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L14](../server/src/main/java/com/naisa/model/vo/VenueInfoResponse.java#L14) | Andrew Hsu, Codex, Vincent Lai, Codex | Describes a venue's location, offered sports, and weekly opening intervals. |
| [L32](../server/src/main/java/com/naisa/model/vo/VenueInfoResponse.java#L32) | Andrew Hsu, Codex | Describes a sport offered at this venue, with its hourly price in dollars and indoor status. |
| [L47](../server/src/main/java/com/naisa/model/vo/VenueInfoResponse.java#L47) | Andrew Hsu, Codex | Describes one weekly opening interval in the venue's local time. |

### ClientRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L17](../server/src/main/java/com/naisa/repository/ClientRepository.java#L17) | James Chen | Retrieves a client by an API key. |

### CrowdReportRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L14](../server/src/main/java/com/naisa/repository/CrowdReportRepository.java#L14) | Emily Wang | Provides persistence access to crowd reports. |
| [L29](../server/src/main/java/com/naisa/repository/CrowdReportRepository.java#L29) | Emily Wang, Claude Code | Finds each reporter's most recent report for a venue since the cutoff time. |

### OpeningIntervalRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L15](../server/src/main/java/com/naisa/repository/OpeningIntervalRepository.java#L15) | Emily Wang, Andrew Hsu, Codex | Provides access to opening hours for each venue. |
| [L27](../server/src/main/java/com/naisa/repository/OpeningIntervalRepository.java#L27) | Andrew Hsu, Codex | Retrieves opening intervals for the supplied venue identifiers. |

### ReviewRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L17](../server/src/main/java/com/naisa/repository/ReviewRepository.java#L17) | Vincent Lai, Codex | Provides persistence access to ratings and comments submitted by external users. |
| [L32](../server/src/main/java/com/naisa/repository/ReviewRepository.java#L32) | Vincent Lai, Codex | Finds a user's existing review for the requested venue and optional sport. |

### SportRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L13](../server/src/main/java/com/naisa/repository/SportRepository.java#L13) | Emily Wang, Andrew Hsu, Codex | Provides access to registered sports. |

### VenueRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/main/java/com/naisa/repository/VenueRepository.java#L16) | Vincent Lai, Codex | Provides persistence access to venue records. |
| [L29](../server/src/main/java/com/naisa/repository/VenueRepository.java#L29) | Vincent Lai, Codex | Finds a venue and acquires a write lock on its database row. |

### VenueSportRepository.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L17](../server/src/main/java/com/naisa/repository/VenueSportRepository.java#L17) | Vincent Lai, Andrew Hsu, Codex | Provides persistence access to the sports offered at individual venues. |
| [L29](../server/src/main/java/com/naisa/repository/VenueSportRepository.java#L29) | Andrew Hsu, Codex | Finds all sports offered at a venue, with the associated sport records loaded. |
| [L43](../server/src/main/java/com/naisa/repository/VenueSportRepository.java#L43) | Vincent Lai, Codex | Finds the offering that connects a venue to a sport. |
| [L53](../server/src/main/java/com/naisa/repository/VenueSportRepository.java#L53) | Andrew Hsu, Codex | Finds offerings for the requested sport within the hourly budget. |
| [L63](../server/src/main/java/com/naisa/repository/VenueSportRepository.java#L63) | Andrew Hsu, Codex | Finds all offerings for a sport before distance, price, or opening-hour checks. |

### ClientService.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L27](../server/src/main/java/com/naisa/service/ClientService.java#L27) | James Chen | Retrieves a client by an API key. |
| [L43](../server/src/main/java/com/naisa/service/ClientService.java#L43) | James Chen | Creates a client. |

### CrowdReportService.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L31](../server/src/main/java/com/naisa/service/CrowdReportService.java#L31) | Emily Wang, Codex | Handles crowd reports submitted by clients. |
| [L49](../server/src/main/java/com/naisa/service/CrowdReportService.java#L49) | Emily Wang, Codex | Creates a crowd report service. |
| [L73](../server/src/main/java/com/naisa/service/CrowdReportService.java#L73) | Emily Wang, Codex, James Chen | Saves a new crowd report for a venue. |
| [L116](../server/src/main/java/com/naisa/service/CrowdReportService.java#L116) | Emily Wang, Claude Code | Returns the current crowd status of a venue or one of its sports. |

### VenueComparisonService.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L32](../server/src/main/java/com/naisa/service/VenueComparisonService.java#L32) | Emily Wang, Codex, Andrew Hsu, Codex | Compares venues offering the same sport using location, price, opening hours, and crowd level. |
| [L54](../server/src/main/java/com/naisa/service/VenueComparisonService.java#L54) | Emily Wang, Codex, Claude Code | Creates the service with the repositories required for venue comparison. |
| [L79](../server/src/main/java/com/naisa/service/VenueComparisonService.java#L79) | Emily Wang, Codex, Claude Code, Andrew Hsu, Codex, Vincent Lai, Codex | Compares the requested venues and identifies the nearest, cheapest, and least crowded ones. |
| [L171](../server/src/main/java/com/naisa/service/VenueComparisonService.java#L171) | Emily Wang, Codex, Claude Code, Andrew Hsu, Codex | Checks whether any opening interval contains the given local date and time. |

### VenueService.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L51](../server/src/main/java/com/naisa/service/VenueService.java#L51) | Vincent Lai, Codex, Andrew Hsu, Codex | Coordinates venue searches, information, creation, and user reviews through repositories. |
| [L76](../server/src/main/java/com/naisa/service/VenueService.java#L76) | Vincent Lai, Codex, Andrew Hsu, Codex | Creates the service with the repositories required for venue searches and submissions. |
| [L101](../server/src/main/java/com/naisa/service/VenueService.java#L101) | Andrew Hsu, Codex, Vincent Lai, Codex | Retrieves a venue's location, offered sports, and weekly opening intervals. |
| [L156](../server/src/main/java/com/naisa/service/VenueService.java#L156) | Andrew Hsu, Codex, Vincent Lai, Codex | Finds open venues for one origin or a group within the requested radius and hourly budget. |
| [L227](../server/src/main/java/com/naisa/service/VenueService.java#L227) | Andrew Hsu, Codex | Converts a single coordinate pair to a one-member list, or uses the supplied member list. |
| [L249](../server/src/main/java/com/naisa/service/VenueService.java#L249) | Andrew Hsu, Codex, Vincent Lai, Codex | Finds alternatives by changing only the search values permitted by the client. |
| [L346](../server/src/main/java/com/naisa/service/VenueService.java#L346) | Andrew Hsu, Codex | Checks the original search time and sport for either search operation. |
| [L366](../server/src/main/java/com/naisa/service/VenueService.java#L366) | Andrew Hsu, Codex | Builds unique future date and time suggestions within the permitted windows. |
| [L401](../server/src/main/java/com/naisa/service/VenueService.java#L401) | Andrew Hsu, Codex | Counts actual changes from the original search, independent of granted permissions. |
| [L427](../server/src/main/java/com/naisa/service/VenueService.java#L427) | Andrew Hsu, Codex | Checks whether a weekly opening interval contains the requested local date and time. |
| [L456](../server/src/main/java/com/naisa/service/VenueService.java#L456) | Vincent Lai, Codex | Creates a venue and records the authenticated client who submitted it. |
| [L498](../server/src/main/java/com/naisa/service/VenueService.java#L498) | Vincent Lai, Codex | Saves a user's rating and comment for a venue or one of its registered sports. |
| [L542](../server/src/main/java/com/naisa/service/VenueService.java#L542) | James Chen. | Gets a sport instance by a venue ID and a sport ID. |

### Beans.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L11](../server/src/main/java/com/naisa/util/Beans.java#L11) | James Chen | A utility class containing a collection of helper functions for JavaBeans. |
| [L29](../server/src/main/java/com/naisa/util/Beans.java#L29) | James Chen | Creates a JavaBean instance by the given JavaBean class, which must be annotated by {@code JavaBean}. |

### VenueSportPersistenceTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L16](../server/src/test/java/com/naisa/model/entity/VenueSportPersistenceTest.java#L16) | Vincent Lai, Codex | Checks that lighting availability survives database writes and subsequent entity reads. |
| [L26](../server/src/test/java/com/naisa/model/entity/VenueSportPersistenceTest.java#L26) | Vincent Lai, Codex | Verifies the default unlit state and an explicitly enabled lighting value through JPA. |

### NumericRequestTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L37](../server/src/test/java/com/naisa/model/NumericRequestTest.java#L37) | Vincent Lai, Codex | Verifies numeric request binding, validation, and persistence using the application's JSON mapper. |
| [L50](../server/src/test/java/com/naisa/model/NumericRequestTest.java#L50) | Vincent Lai, Codex | Checks coordinate bounds and nonfinite values on every request bean that carries coordinates. |
| [L88](../server/src/test/java/com/naisa/model/NumericRequestTest.java#L88) | Vincent Lai, Codex | Ensures both supported rating boundaries bind to Short and pass validation. |
| [L103](../server/src/test/java/com/naisa/model/NumericRequestTest.java#L103) | Vincent Lai, Codex | Rejects decimal tokens and values outside SMALLINT before they can be rounded or wrapped. |
| [L117](../server/src/test/java/com/naisa/model/NumericRequestTest.java#L117) | Vincent Lai, Codex | Requires ratings and rejects integers outside the one-to-five range after binding. |
| [L131](../server/src/test/java/com/naisa/model/NumericRequestTest.java#L131) | Vincent Lai, Codex | Saves JSON coordinates and a Short rating through the services and verifies the database values. |

### CrowdReportServiceTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L46](../server/src/test/java/com/naisa/service/CrowdReportServiceTest.java#L46) | Emily Wang, Claude Code | Unit tests for {@link CrowdReportService}. |

### GroupVenueSearchTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L45](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L45) | Andrew Hsu, Codex | Tests group venue searches with mocked repositories and a fixed New York time. |
| [L67](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L67) | Andrew Hsu, Codex | Creates a valid group request with no individual coordinate pair. |
| [L85](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L85) | Andrew Hsu, Codex | Restores normal date-time behavior after each test. |
| [L95](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L95) | Andrew Hsu, Codex | Gives a one-member group the same response as an individual coordinate pair. |
| [L124](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L124) | Andrew Hsu, Codex | Averages both coordinates and applies the radius only to the center-to-venue distance. |
| [L146](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L146) | Andrew Hsu, Codex | Excludes a venue outside the center radius even when it is at one member's location. |
| [L163](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L163) | Andrew Hsu, Codex | Keeps member distances in input order and uses their arithmetic average. |
| [L195](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L195) | Andrew Hsu, Codex | Counts members at the same location separately and sorts by average member distance. |
| [L219](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L219) | Andrew Hsu, Codex | Uses venue ID to order equal average distances. |
| [L235](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L235) | Andrew Hsu, Codex | Supports the maximum valid group size and includes a venue at a zero-radius center. |
| [L256](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L256) | Andrew Hsu, Codex | Rejects a past group search before consulting repositories. |
| [L276](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L276) | Andrew Hsu, Codex | Rejects an unknown sport before retrieving group search candidates. |
| [L295](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L295) | Andrew Hsu, Codex | Supplies repository results already filtered by sport and budget. |
| [L306](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L306) | Andrew Hsu, Codex | Supplies open venues that are within the configured radius. |
| [L329](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L329) | Andrew Hsu, Codex | Creates one member location with double-precision coordinates. |
| [L343](../server/src/test/java/com/naisa/service/GroupVenueSearchTest.java#L343) | Andrew Hsu, Codex | Creates an affordable offering at the specified coordinates. |

### VenueAlternativesTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L49](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L49) | Andrew Hsu, Codex | Tests permitted venue alternatives through the public service method without a database. |
| [L70](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L70) | Andrew Hsu, Codex | Fixes New York time and creates a valid request with one permitted change. |
| [L90](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L90) | Andrew Hsu, Codex | Releases the clock mock after each case. |
| [L101](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L101) | Andrew Hsu, Codex | Preserves original matches, maps venue fields, and avoids duplicate results from repeated hours. |
| [L129](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L129) | Andrew Hsu, Codex | Applies permissions, inclusive budget limits, and the five-kilometer radius increase. |
| [L174](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L174) | Andrew Hsu, Codex | Includes both three-day boundaries and orders equal time differences by earlier date. |
| [L200](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L200) | Andrew Hsu, Codex | Preserves minute, second, and nanosecond precision for all permitted hour offsets. |
| [L225](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L225) | Andrew Hsu, Codex | Allows midnight crossings only with date permission and keeps dates inside the date window. |
| [L263](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L263) | Andrew Hsu, Codex | Removes past dates and times while retaining a suggestion exactly at the fixed current time. |
| [L288](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L288) | Andrew Hsu, Codex | Counts actual changes rather than permissions before comparing distances or prices. |
| [L314](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L314) | Andrew Hsu, Codex | Breaks equal-change and equal-time ties by distance, then price, then venue ID. |
| [L341](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L341) | Andrew Hsu, Codex | Applies the ten-result limit after sorting rather than taking repository order. |
| [L367](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L367) | Andrew Hsu, Codex | Returns no alternatives when nearby affordable venues are closed or have no hours. |
| [L386](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L386) | Andrew Hsu, Codex | Checks each service error before unnecessary repository work. |
| [L410](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L410) | Andrew Hsu, Codex | Checks the public error code and status. |
| [L425](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L425) | Andrew Hsu, Codex | Supplies all sport offerings before the service applies distance and budget filters. |
| [L435](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L435) | Andrew Hsu, Codex | Supplies opening intervals for the affordable nearby venues. |
| [L446](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L446) | Andrew Hsu, Codex | Supplies a venue open for 24 hours on every weekday. |
| [L461](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L461) | Andrew Hsu, Codex | Creates a sport offering at the selected latitude and price. |
| [L479](../server/src/test/java/com/naisa/service/VenueAlternativesTest.java#L479) | Andrew Hsu, Codex | Creates a weekly opening interval with local opening and closing times. |

### VenueComparisonServiceTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L50](../server/src/test/java/com/naisa/service/VenueComparisonServiceTest.java#L50) | Emily Wang, Claude Code, Andrew Hsu, Codex, Vincent Lai, Codex | Unit tests for {@link VenueComparisonService}. |
| [L67](../server/src/test/java/com/naisa/service/VenueComparisonServiceTest.java#L67) | Emily Wang, Claude Code, Vincent Lai, Codex | Creates a request using double-precision coordinates for the given venue IDs. |
| [L228](../server/src/test/java/com/naisa/service/VenueComparisonServiceTest.java#L228) | Emily Wang, Claude Code, Andrew Hsu, Codex | Checks open, closed, and unknown venues at a fixed time. |
| [L271](../server/src/test/java/com/naisa/service/VenueComparisonServiceTest.java#L271) | Andrew Hsu, Codex | Includes 24 hours from an equal opening and closing time, with an exclusive closing boundary. |

### VenueSearchTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L45](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L45) | Andrew Hsu, Codex | Tests individual venue searches with mocked repositories and a fixed New York time. |
| [L67](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L67) | Andrew Hsu, Codex | Fixes the clock and creates a valid individual search request. |
| [L86](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L86) | Andrew Hsu, Codex | Restores real date-time behavior after each test. |
| [L96](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L96) | Andrew Hsu, Codex | Returns venue fields and equal individual, center, and average distances. |
| [L123](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L123) | Andrew Hsu, Codex | Includes zero distance at a zero radius and excludes venues beyond the radius. |
| [L151](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L151) | Andrew Hsu, Codex | Requests the selected sport and inclusive budget, including a zero budget. |
| [L169](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L169) | Andrew Hsu, Codex | Stops before fetching hours when every offering is outside the search radius. |
| [L184](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L184) | Andrew Hsu, Codex | Checks daytime, overnight, week boundary, and 24-hour intervals through the public method. |
| [L225](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L225) | Andrew Hsu, Codex | Uses any matching interval and excludes venues with no stored hours. |
| [L245](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L245) | Andrew Hsu, Codex | Sorts by distance first and by venue ID when distances are equal. |
| [L266](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L266) | Andrew Hsu, Codex | Rejects past dates and earlier times today before consulting repositories. |
| [L287](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L287) | Andrew Hsu, Codex | Accepts a request exactly at the fixed current time. |
| [L302](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L302) | Andrew Hsu, Codex | Rejects an unknown sport before fetching offerings or hours. |
| [L322](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L322) | Andrew Hsu, Codex | Supplies offerings already filtered by sport and budget, as required by the repository contract. |
| [L333](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L333) | Andrew Hsu, Codex | Creates an offering north of the fixed search origin. |
| [L351](../server/src/test/java/com/naisa/service/VenueSearchTest.java#L351) | Andrew Hsu, Codex | Creates a weekly opening interval for an offering's venue. |

### VenueServiceTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L41](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L41) | Andrew Hsu, Codex | Tests venue service behavior with mocked repositories and no database. |
| [L58](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L58) | Andrew Hsu, Codex | Tests the venue information operation. |
| [L67](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L67) | Andrew Hsu, Codex | Creates a venue shared by the successful lookup cases. |
| [L83](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L83) | Andrew Hsu, Codex | Returns venue fields and all sports, sorted by sport ID. |
| [L115](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L115) | Andrew Hsu, Codex | Sorts hours by weekday, opening time, and closing time, preserving overnight hours. |
| [L146](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L146) | Andrew Hsu, Codex | Returns empty lists when either or both related record sets are absent. |
| [L167](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L167) | Andrew Hsu, Codex | Rejects an unknown venue before querying its sports or hours. |
| [L185](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L185) | Andrew Hsu, Codex | Creates a sport offering with distinct response fields. |
| [L203](../server/src/test/java/com/naisa/service/VenueServiceTest.java#L203) | Andrew Hsu, Codex | Creates an opening interval in the venue's local time. |

### VenueSubmissionServiceTest.java

| Source | Recorded authors/tools | Javadoc scope |
| --- | --- | --- |
| [L48](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L48) | Vincent Lai, Codex | Tests submission business rules using mocked repositories, without Spring or a database. |
| [L69](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L69) | Vincent Lai, Codex | Creates the authenticated client supplied by the controller. |
| [L79](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L79) | Vincent Lai, Codex | Tests venue creation and rejection before persistence. |
| [L88](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L88) | Vincent Lai, Codex | Creates a valid request with whitespace to exercise normalization. |
| [L107](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L107) | Vincent Lai, Codex | Checks the stored fields, attribution, and database-assigned response identifier. |
| [L133](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L133) | Vincent Lai, Codex | Rejects unrecognized and fixed-offset zones without writing anything. |
| [L153](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L153) | Vincent Lai, Codex | Repeating the same payload creates separate venues rather than deduplicating them. |
| [L175](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L175) | Vincent Lai, Codex | Tests review creation, updates, scope selection, and business-rule failures. |
| [L185](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L185) | Vincent Lai, Codex | Creates a whole-venue review request with an exact external user identifier. |
| [L203](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L203) | Vincent Lai, Codex | Creates a review for the venue or its selected sport and locks before querying. |
| [L245](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L245) | Vincent Lai, Codex | Updates in place while preserving the original identity and creation timestamp. |
| [L276](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L276) | Vincent Lai, Codex | Includes the current client and exact user ID in the existing-review lookup. |
| [L293](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L293) | Vincent Lai, Codex | Rejects an absent venue before querying offerings or reviews. |
| [L307](../server/src/test/java/com/naisa/service/VenueSubmissionServiceTest.java#L307) | Vincent Lai, Codex | Rejects a sport not offered at the venue without persisting a review. |
