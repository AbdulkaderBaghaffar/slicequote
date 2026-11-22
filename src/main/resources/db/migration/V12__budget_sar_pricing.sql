-- Budget SAR pricing: 3 SAR/hour, 2 SAR base fee, 8 SAR minimum.
UPDATE pricing_rate
SET hourly_rate = 3.00,
    base_fee = 2.00,
    min_floor = 8.00
WHERE mode = 'MACHINE_TIME';

-- Realistic retail filament rates for the active catalogue.
UPDATE material SET cost_per_gram = 0.10 WHERE lower(name) = 'pla';
UPDATE material SET cost_per_gram = 0.12 WHERE lower(name) = 'petg';
UPDATE material SET cost_per_gram = 0.11 WHERE lower(name) = 'abs';
UPDATE material SET cost_per_gram = 0.18 WHERE lower(name) = 'tpu';
