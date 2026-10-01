# T1: Project Proposal

<small>By **James Chen, Andrew Hsu, Vincent Lai, Emily Wang** Last modified on Sep 30, 2026</small>

## Part 1: Team

Nothing has changed.

## Part 2: Service Functionality

### 1. Find Venues

**Operation:** `findVenues`

**Inputs**
- `location: Location` - origin location for the search
- `sport: Sport` - origin location for the search
- `date: LocalDate` - origin location for the search
- `time: LocalTime` - origin location for the search
- `maxDistance: double` - origin location for the search
- `budget: double` - origin location for the search

**Computation**
- Converts the location into geographic coordinates.
- Filters stored venues by supported sport.
- Computers the distance between the requested location and each candidate venue.
- Filters venues by distance, operating hours, price, and availability.

**Returns**
- `List<VenueSearchResult>`

Each result contains:
- venue identifier
- venue name
- distance
- estimated price
- relevant facility information
- availability information

<!-- TODO: findAlternatives - if nothing within findVenues fits, suggest alternatives  -->
<!-- TODO: findGroupVenue - take multiple people's locations w/ sport, day, time, return set of options  -->
<!-- TODO: getVenueInfo - input venue, return information venue (sports, hours, field type, etc.)  -->
<!-- TODO: compareVenues - input multiple venues (2, optional 3+?) return info about all  -->
<!-- TODO: venueStatus - similar to waze, user reported status; empty, ... ,packed; time of report  -->
<!-- TODO: getVenueStatus - other users can get reported venue status  -->

## Part 3: Client Programs

<!-- TODO: Fitness training platforms (e.g. Strava) could recommend places for given activity  -->
<!-- TODO: Sports organizer platforms (e.g. Volo, GoodRec) recommend for their sports offerings  -->


## Part 4: Development Tools

<!-- TODO: Add the development tools section here. -->