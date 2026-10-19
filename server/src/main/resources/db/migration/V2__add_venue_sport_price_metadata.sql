ALTER TABLE venue_sports
ADD COLUMN surface_type VARCHAR(64),
ADD COLUMN currency VARCHAR(3),
ADD COLUMN price_unit VARCHAR(32);

COMMENT ON COLUMN venue_sports.surface_type
IS 'Type of field surface for this sport';
COMMENT ON COLUMN venue_sports.currency
IS 'Currency code for the stored price; NULL means unknown';
COMMENT ON COLUMN venue_sports.price_unit
IS 'Unit for the stored price, such as PER_COURT_HOUR; NULL means unknown';
