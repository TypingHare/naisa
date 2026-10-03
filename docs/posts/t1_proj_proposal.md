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
- Computers the distance between the requested location and each candidate venue.
- Filters venues by distance, operating hours, price, and availability.

**Returns**
- a list of venue results, each containing the following entry results:
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

### Fitness Platforms

Fitness tracking platforms such as Strava or Nike Run Club are primarily
designed to record workouts, analyze fitness activity, and help users plan
training. These programs could call our service when they need to identify
sports venues appropriate for a planned activity. For example, a running
application could request nearby venues that contain a running track and 
satisfy constraints such as distance, operating hours, or lighting.

### Sports Pickup Organizer Platforms 

Pickup and recreational sports platforms such as Volo or GoodRec are designed
to organize games, leagues, and other group activities. These programs could
call our service to identify suitable practice or meetup venues for a sport,
particularly outside of their own scheduled events. A client could provide the
sport, participant locations, desired time, and budget, and use the returned
venue options when coordinating a group activity.

### NYC Department of Parks & Recreation

An NYC Parks and Recreation website serves a broader informational purpose,
including helping residents learn about parks, facilities, programs, and public
recreation resources. The website could call our service to provide more
specialized sports-facility discovery than its general park search. For
example, it could request venues that support a particular sport and satisfy
location, time, or facility constraints, then incorporate those results into 
its existing recreation information.

## Part 4: Development Tools

<!-- TODO: Add the development tools section here. -->