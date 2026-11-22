-- Cheaper SAR pricing: 10 SAR/hour, 1 SAR base fee, 3 SAR minimum.
UPDATE pricing_rate
SET hourly_rate = 10.00,
    base_fee = 1.00,
    min_floor = 3.00
WHERE mode = 'MACHINE_TIME';
