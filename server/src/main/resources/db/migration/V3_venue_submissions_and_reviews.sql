-- Record who submitted a venue.
ALTER TABLE venues
ADD COLUMN created_by_client_id BIGINT REFERENCES clients(id),
ADD COLUMN created_at TIMESTAMPTZ;

-- Existing venues retain an unknown creation time.
-- Future inserts receive a database-generated timestamp.
ALTER TABLE venues
ALTER COLUMN created_at SET DEFAULT current_timestamp;

-- Treat NULL sport IDs as the same review scope.
ALTER TABLE reviews
ADD CONSTRAINT reviews_one_per_user_scope
    UNIQUE NULLS NOT DISTINCT (
        client_id,
        venue_id,
        sport_id
    );

