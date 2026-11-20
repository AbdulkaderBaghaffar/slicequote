ALTER TABLE material ADD COLUMN active boolean NOT NULL DEFAULT true;

UPDATE material
SET active = false
WHERE lower(name) NOT IN ('pla', 'petg', 'tpu', 'abs');
