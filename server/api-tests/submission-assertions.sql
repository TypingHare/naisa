-- Read-only persistence checks after the complete operations 8-9 collection.
-- Pass its createdVenueId collection variable using psql -v venue_id=123.
-- AI-generated. Author: Vincent Lai, Codex.
\set ON_ERROR_STOP on

BEGIN;
CREATE TEMP TABLE submission_test_context ON COMMIT DROP AS
SELECT id, created_by_client_id FROM venues WHERE id = :'venue_id'::BIGINT;

DO $$
DECLARE
    submitted_venue_id BIGINT;
    submitting_client_id BIGINT;
    saved_review reviews%ROWTYPE;
BEGIN
    SELECT id, created_by_client_id INTO STRICT submitted_venue_id, submitting_client_id
    FROM submission_test_context;

    IF NOT EXISTS (
        SELECT 1 FROM venues
        WHERE id = submitted_venue_id AND created_at IS NOT NULL
          AND name = 'Submission API Court' AND address = '1 Test Street'
    ) THEN
        RAISE EXCEPTION 'Venue normalization, client attribution, or creation timestamp is missing';
    END IF;

    -- Only the original, duplicate, and two boundary submissions should have succeeded.
    IF (SELECT COUNT(*) FROM venues WHERE created_by_client_id = submitting_client_id) <> 4 THEN
        RAISE EXCEPTION 'Expected four valid creations and no invalid creations';
    END IF;

    SELECT * INTO STRICT saved_review FROM reviews
    WHERE venue_id = submitted_venue_id AND client_id = submitting_client_id
      AND external_user_id = 'submission-user-a' AND sport_id IS NULL;
    IF saved_review.rating <> 4 OR saved_review.comment_content <> 'Final whole-venue feedback.'
        OR saved_review.created_at IS NULL OR saved_review.updated_at < saved_review.created_at THEN
        RAISE EXCEPTION 'Review update was not persisted correctly';
    END IF;

    IF (SELECT COUNT(*) FROM reviews WHERE venue_id = submitted_venue_id) <> 4 THEN
        RAISE EXCEPTION 'Expected three external users plus one independent client review';
    END IF;

    IF (SELECT COUNT(*) FROM reviews
        WHERE venue_id = submitted_venue_id AND client_id = submitting_client_id
          AND external_user_id IN ('submission-user-b', ' submission-user-a ')
          AND comment_content = 'Excellent court.' AND rating = 5) <> 2 THEN
        RAISE EXCEPTION 'Independent users or normalized comments were not persisted';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM reviews
        WHERE venue_id = submitted_venue_id AND client_id <> submitting_client_id
          AND external_user_id = 'submission-user-a' AND rating = 2) THEN
        RAISE EXCEPTION 'The second client review was not isolated';
    END IF;

    -- The collection uses fixture venue/sport 9001 by default.
    IF (SELECT COUNT(*) FROM reviews
        WHERE venue_id = 9001 AND client_id = submitting_client_id) <> 2
        OR NOT EXISTS (SELECT 1 FROM reviews
            WHERE venue_id = 9001 AND client_id = submitting_client_id AND sport_id = 9001
              AND rating = 3 AND comment_content = 'Updated sport feedback.') THEN
        RAISE EXCEPTION 'Whole-venue and sport review scopes were not independent';
    END IF;

    RAISE NOTICE 'Submission persistence assertions passed';
END;
$$;

ROLLBACK;
