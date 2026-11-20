-- Seed the rest of the material catalogue used by the quote UI.
-- V4 already seeds PLA; these guards keep the migration safe if a name exists.
INSERT INTO material (name, cost_per_gram)
SELECT 'ABS', 0.04
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'abs');

INSERT INTO material (name, cost_per_gram)
SELECT 'PETG', 0.05
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'petg');

INSERT INTO material (name, cost_per_gram)
SELECT 'TPU', 0.06
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'tpu');

INSERT INTO material (name, cost_per_gram)
SELECT 'ASA', 0.05
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'asa');

INSERT INTO material (name, cost_per_gram)
SELECT 'Nylon', 0.08
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'nylon');

INSERT INTO material (name, cost_per_gram)
SELECT 'PC', 0.09
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'pc');

INSERT INTO material (name, cost_per_gram)
SELECT 'Resin', 0.12
WHERE NOT EXISTS (SELECT 1 FROM material WHERE lower(name) = 'resin');
