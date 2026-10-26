-- Database checks supplement the HTTP assertions for behavior with no read endpoint.
-- Author: Andrew Hsu, Codex
DO $$
BEGIN
  IF (SELECT count(*) FROM reviews WHERE venue_id = 9001) <> 4 THEN
    RAISE EXCEPTION 'Expected four independent review scopes';
  END IF;
  IF (SELECT count(*) FROM reviews
      WHERE venue_id = 9001 AND external_user_id = 'review-alice'
        AND sport_id IS NULL AND rating = 5 AND comment_content = 'Updated review') <> 1 THEN
    RAISE EXCEPTION 'The original review must be updated and its comment trimmed';
  END IF;
  IF (SELECT count(*) FROM crowd_reports r JOIN venues v ON r.venue_id = v.id
      WHERE v.name = 'API Test Fresh Venue' AND r.external_user_id = 'u1') <> 2 THEN
    RAISE EXCEPTION 'Both submissions from u1 must remain in the database';
  END IF;
END;
$$;
