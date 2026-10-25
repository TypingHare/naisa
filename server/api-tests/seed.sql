-- Test data for the Postman API tests of Operations 5-7
-- (naisa-venue-ops.postman_collection.json).
--
-- Run it once before running the collection:
--   psql -d naisa -f server/api-tests/seed.sql
--
-- The script can be run again at any time. It removes crowd reports left on the test venues by
-- earlier runs, and it uses IDs in the 9000 range so that it does not clash with venues created
-- through the API.
--
-- Author: Emily Wang, Claude Code

-- Remove crowd reports from earlier runs so that every run starts from the same state.
DELETE FROM crowd_reports WHERE venue_id IN (9001, 9002, 9003);

-- Venues near Columbia University. The API tests search from (40.80, -73.96).
INSERT INTO venues (id, name, address, latitude, longitude, timezone)
VALUES
    (9001, 'API Test Gym A', '1 Test St, New York, NY 10027', 40.81, -73.96, 'America/New_York'),
    (9002, 'API Test Gym B', '2 Test St, New York, NY 10025', 40.75, -73.98, 'America/New_York'),
    (9003, 'API Test Gym C', '3 Test St, New York, NY 10024', 40.70, -73.99, 'America/New_York')
ON CONFLICT (id) DO NOTHING;

INSERT INTO sports (id, name)
VALUES
    (9001, 'API Test Basketball'),
    (9002, 'API Test Tennis')
ON CONFLICT (id) DO NOTHING;

-- Basketball is offered at all three venues; tennis only at venue 9003.
INSERT INTO venue_sports (id, venue_id, sport_id, price, is_indoor)
VALUES
    (9001, 9001, 9001, 20.00, TRUE),
    (9002, 9002, 9001, 15.00, FALSE),
    (9003, 9003, 9001, 30.00, TRUE),
    (9004, 9003, 9002, 25.00, FALSE)
ON CONFLICT (id) DO NOTHING;

-- Venue 9001 is open all day, every day. Venues 9002 and 9003 have no opening hours.
INSERT INTO opening_intervals (id, venue_id, day_of_week, opens_at, closes_at)
VALUES
    (9001, 9001, 'MONDAY', '00:00', '23:59:59'),
    (9002, 9001, 'TUESDAY', '00:00', '23:59:59'),
    (9003, 9001, 'WEDNESDAY', '00:00', '23:59:59'),
    (9004, 9001, 'THURSDAY', '00:00', '23:59:59'),
    (9005, 9001, 'FRIDAY', '00:00', '23:59:59'),
    (9006, 9001, 'SATURDAY', '00:00', '23:59:59'),
    (9007, 9001, 'SUNDAY', '00:00', '23:59:59')
ON CONFLICT (id) DO NOTHING;
