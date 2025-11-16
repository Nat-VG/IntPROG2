USE BaseVehiculos;

-- ============================================
-- INSERTS: VEHICULO
-- ============================================

INSERT INTO vehiculo (dominio, marca, modelo, anio, nroChasis)
VALUES
('ABC123', 'Toyota', 'Corolla', 2020, 'CH-0001'),
('CDE456', 'Ford',   'Fiesta',  2015, 'CH-0002'),
('FGH789', 'Volkswagen', 'Gol', 2018, 'CH-0003');

-- ============================================
-- INSERTS: SEGUROVEHICULAR
-- (Recordá que idVehiculo debe existir en la tabla vehiculo)
-- ============================================

INSERT INTO segurovehicular (aseguradora, nroPoliza, cobertura, vencimiento, idVehiculo)
VALUES
('La Caja',  'POL-001', 'TODO_RIESGO',  '2026-01-15', 1),
('Sancor',   'POL-555', 'RC',           '2025-10-01', 2);