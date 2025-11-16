-- ============================================================
--   SCRIPT COMPLETO DE INICIALIZACIÓN DEL TPI 
--   Crea la base, tablas, restricciones e inserta datos.
-- ============================================================

-- 1. BORRAR DB SI EXISTE
DROP DATABASE IF EXISTS BaseVehiculos;

-- 2. CREAR DB
CREATE DATABASE BaseVehiculos;
USE BaseVehiculos;

-- ============================================================
-- 3. TABLA VEHICULO (A)
-- ============================================================
CREATE TABLE vehiculo (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  eliminado TINYINT(1) DEFAULT 0,
  dominio VARCHAR(10) NOT NULL UNIQUE,
  marca VARCHAR(50) NOT NULL,
  modelo VARCHAR(50) NOT NULL,

  -- Rango sincronizado con validaciones en Java
  anio INT CHECK (anio BETWEEN 1950 AND YEAR(CURDATE()) + 1),

  nroChasis VARCHAR(50) UNIQUE
) ENGINE = InnoDB;

-- ============================================================
-- 4. TABLA SEGUROVEHICULAR (B)
-- ============================================================
CREATE TABLE segurovehicular (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  eliminado TINYINT(1) DEFAULT 0,
  aseguradora VARCHAR(80) NOT NULL,
  nroPoliza VARCHAR(50) UNIQUE NOT NULL,
  cobertura ENUM('RC', 'TERCEROS', 'TODO_RIESGO') NOT NULL,
  vencimiento DATE NOT NULL,
  idVehiculo BIGINT NOT NULL UNIQUE,

  CONSTRAINT fk_seguro_vehiculo
    FOREIGN KEY (idVehiculo)
    REFERENCES vehiculo(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;

-- ============================================================
-- 5. INSERTS DE PRUEBA (OPCIONALES PARA CORRECCIÓN)
-- ============================================================

INSERT INTO vehiculo (dominio, marca, modelo, anio, nroChasis)
VALUES 
('AA111AA', 'Toyota', 'Corolla', 2020, 'CHS-TY-001'),
('AB222CD', 'Honda', 'Civic', 2021, 'CHS-HD-002'),
('AC333EF', 'Ford', 'Focus', 2019, 'CHS-FD-003');

INSERT INTO segurovehicular
(aseguradora, nroPoliza, cobertura, vencimiento, idVehiculo)
VALUES
('Sancor', 'POL123', 'RC', DATE_ADD(CURDATE(), INTERVAL 120 DAY), 1),
('LaCaja', 'POL555', 'TERCEROS', DATE_ADD(CURDATE(), INTERVAL 200 DAY), 2);

-- Vehículo 3 queda sin seguro para probar opción 6
