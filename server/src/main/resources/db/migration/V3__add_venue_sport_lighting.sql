-- Existing offerings have no lighting recorded; new inserts use the same
-- default.
ALTER TABLE venue_sports
ADD COLUMN has_lighting BOOLEAN;

COMMENT ON COLUMN venue_sports.has_lighting
IS 'Whether lighting is available for this sport at the venue. NULL indicates '
'unknown.';
