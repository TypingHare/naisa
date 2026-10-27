-- Each client's external users have independent reviews within a venue and sport.
-- Author: Andrew Hsu, Codex
ALTER TABLE reviews DROP CONSTRAINT reviews_one_per_user_scope;
ALTER TABLE reviews ADD CONSTRAINT reviews_one_per_user_scope
  UNIQUE NULLS NOT DISTINCT (client_id, external_user_id, venue_id, sport_id);
