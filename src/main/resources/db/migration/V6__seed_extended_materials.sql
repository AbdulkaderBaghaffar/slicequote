-- Extended catalogue: engineering, support, and high-temperature materials.
-- Guards keep this safe if a name was inserted manually.
INSERT INTO material (name, cost_per_gram)
SELECT 'PLA-CF', 0.06
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'pla-cf');

INSERT INTO material (name, cost_per_gram)
SELECT 'PA-CF', 0.14
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'pa-cf');

INSERT INTO material (name, cost_per_gram)
SELECT 'PVA', 0.10
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'pva');

INSERT INTO material (name, cost_per_gram)
SELECT 'Wood', 0.07
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'wood');

INSERT INTO material (name, cost_per_gram)
SELECT 'HIPS', 0.04
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'hips');

INSERT INTO material (name, cost_per_gram)
SELECT 'PEI', 0.25
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'pei');
