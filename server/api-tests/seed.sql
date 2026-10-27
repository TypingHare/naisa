-- Test data for the complete Postman API suite. Use only a disposable test database.
-- (naisa-venue-ops.postman_collection.json).
--
-- Run it once before running the collection:
--   psql -d naisa -f server/api-tests/seed.sql
--
-- The script can be run again at any time. It removes crowd reports left on the test venues by
-- earlier runs, and it uses IDs in the 9000 range so that it does not clash with venues created
-- through the API.
--
-- Author: Emily Wang, Claude Code, Andrew Hsu, Codex

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
    (9001, 9001, 'MONDAY', '00:00', '00:00'),
    (9002, 9001, 'TUESDAY', '00:00', '00:00'),
    (9003, 9001, 'WEDNESDAY', '00:00', '00:00'),
    (9004, 9001, 'THURSDAY', '00:00', '00:00'),
    (9005, 9001, 'FRIDAY', '00:00', '00:00'),
    (9006, 9001, 'SATURDAY', '00:00', '00:00'),
    (9007, 9001, 'SUNDAY', '00:00', '00:00')
ON CONFLICT (id) DO NOTHING;


-- Dedicated search fixtures prevent the comparison venues from changing search results.
INSERT INTO sports (id, name) VALUES
  (9010, 'API Test Search'), (9011, 'API Test Overnight'),
  (9012, 'API Test Daytime'), (9013, 'API Test Full Day'),
  (9014, 'API Test Weekly'), (9050, 'API Test No Offerings')
ON CONFLICT (id) DO NOTHING;

INSERT INTO venues (id, name, address, latitude, longitude, timezone) VALUES
  (9010, 'API Test Search Origin', '10 Test St', 40.00, -73.00, 'America/New_York'),
  (9011, 'API Test Search Near', '11 Test St', 40.01, -73.00, 'America/New_York'),
  (9012, 'API Test Search Center', '12 Test St', 40.02, -73.00, 'America/New_York'),
  (9013, 'API Test Closed', '13 Test St', 40.00, -73.00, 'America/New_York'),
  (9014, 'API Test Missing Hours', '14 Test St', 40.00, -73.00, 'America/New_York'),
  (9015, 'API Test Overnight', '15 Test St', 40.00, -73.00, 'America/New_York'),
  (9016, 'API Test Daytime', '16 Test St', 40.00, -73.00, 'America/New_York'),
  (9017, 'API Test Full Day', '17 Test St', 40.00, -73.00, 'America/New_York'),
  (9018, 'API Test Far', '18 Test St', 41.00, -73.00, 'America/New_York'),
  (9019, 'API Test Over Budget', '19 Test St', 40.00, -73.00, 'America/New_York'),
  (9020, 'API Test Weekly', '20 Test St', 40.00, -73.00, 'America/New_York')
ON CONFLICT (id) DO NOTHING;

INSERT INTO venue_sports (id, venue_id, sport_id, price, is_indoor) VALUES
  (9010, 9010, 9010, 10, TRUE), (9011, 9011, 9010, 20, FALSE),
  (9012, 9012, 9010, 30, TRUE), (9013, 9013, 9010, 5, TRUE),
  (9014, 9014, 9010, 0, TRUE), (9015, 9015, 9011, 8, TRUE),
  (9016, 9016, 9012, 0, TRUE), (9017, 9017, 9013, 10, TRUE),
  (9018, 9018, 9010, 10, TRUE), (9019, 9019, 9010, 30.01, TRUE),
  (9020, 9020, 9014, 10, TRUE)
ON CONFLICT (id) DO NOTHING;

INSERT INTO opening_intervals (id, venue_id, day_of_week, opens_at, closes_at)
SELECT 10000 + (v.id - 9010) * 7 + d.number, v.id, d.day,
  CASE v.id WHEN 9013 THEN TIME '14:00' WHEN 9016 THEN TIME '09:00'
    WHEN 9017 THEN TIME '09:00' ELSE TIME '00:00' END,
  CASE v.id WHEN 9013 THEN TIME '15:00' WHEN 9016 THEN TIME '17:00'
    WHEN 9017 THEN TIME '09:00' ELSE TIME '00:00' END
FROM venues v CROSS JOIN (VALUES
  (0, 'MONDAY'), (1, 'TUESDAY'), (2, 'WEDNESDAY'), (3, 'THURSDAY'),
  (4, 'FRIDAY'), (5, 'SATURDAY'), (6, 'SUNDAY')
) AS d(number, day)
WHERE v.id IN (9010, 9011, 9012, 9013, 9016, 9017, 9018, 9019)
ON CONFLICT (id) DO NOTHING;

INSERT INTO opening_intervals (id, venue_id, day_of_week, opens_at, closes_at) VALUES
  (11000, 9015, 'MONDAY', '22:00', '02:00'),
  (11001, 9020, 'MONDAY', '09:00', '17:00')
ON CONFLICT (id) DO NOTHING;

-- An expired report must not affect the comparison or crowd-status response.
INSERT INTO clients (id) VALUES (9000) ON CONFLICT (id) DO NOTHING;
INSERT INTO crowd_reports (id, client_id, external_user_id, venue_id, crowd_level, created_at)
VALUES (9000, 9000, 'expired-reporter', 9003, 'PACKED', current_timestamp - INTERVAL '3 hours')
ON CONFLICT (id) DO NOTHING;

-- Explicit fixture IDs must not collide with IDs assigned by subsequent API requests.
SELECT setval(pg_get_serial_sequence('venues', 'id'), (SELECT max(id) FROM venues));
SELECT setval(pg_get_serial_sequence('sports', 'id'), (SELECT max(id) FROM sports));
SELECT setval(pg_get_serial_sequence('clients', 'id'), (SELECT max(id) FROM clients));
SELECT setval(pg_get_serial_sequence('venue_sports', 'id'), (SELECT max(id) FROM venue_sports));
SELECT setval(pg_get_serial_sequence('opening_intervals', 'id'), (SELECT max(id) FROM opening_intervals));
SELECT setval(pg_get_serial_sequence('crowd_reports', 'id'), (SELECT max(id) FROM crowd_reports));
