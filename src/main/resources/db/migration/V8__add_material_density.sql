ALTER TABLE material ADD COLUMN density numeric(10,3) NOT NULL DEFAULT 1.24;

UPDATE material SET density = 1.24 WHERE lower(name) = 'pla';
UPDATE material SET density = 1.04 WHERE lower(name) = 'abs';
UPDATE material SET density = 1.27 WHERE lower(name) = 'petg';
UPDATE material SET density = 1.21 WHERE lower(name) = 'tpu';
UPDATE material SET density = 1.07 WHERE lower(name) = 'asa';
UPDATE material SET density = 1.14 WHERE lower(name) = 'nylon';
UPDATE material SET density = 1.20 WHERE lower(name) = 'pc';
UPDATE material SET density = 1.10 WHERE lower(name) = 'resin';
UPDATE material SET density = 1.30 WHERE lower(name) = 'pla-cf';
UPDATE material SET density = 1.15 WHERE lower(name) = 'pa-cf';
UPDATE material SET density = 1.23 WHERE lower(name) = 'pva';
UPDATE material SET density = 1.15 WHERE lower(name) = 'wood';
UPDATE material SET density = 1.04 WHERE lower(name) = 'hips';
UPDATE material SET density = 1.27 WHERE lower(name) = 'pei';
