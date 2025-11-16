-- CREACIÓN DE BASE DE DATOS
DROP DATABASE IF EXISTS BaseVehiculos;
CREATE DATABASE BaseVehiculos;

USE BaseVehiculos;

-- 1) TABLA VEHICULO
DROP TABLE IF EXISTS vehiculo;
CREATE TABLE vehiculo (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  eliminado TINYINT(1) DEFAULT 0,
  dominio VARCHAR(10) NOT NULL UNIQUE,
  marca VARCHAR(50) NOT NULL,
  modelo VARCHAR(50) NOT NULL,
  anio INT CHECK (anio BETWEEN 1990 AND 2030),
  nroChasis VARCHAR(50) UNIQUE
) ENGINE = InnoDB;

-- 2) TABLA SEGUROVEHICULAR
DROP TABLE IF EXISTS segurovehicular;
CREATE TABLE segurovehicular (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  eliminado TINYINT(1) DEFAULT 0,
  aseguradora VARCHAR(80) NOT NULL,
  nroPoliza VARCHAR(50) UNIQUE,
  cobertura ENUM('RC', 'TERCEROS', 'TODO_RIESGO') NOT NULL,
  vencimiento DATE NOT NULL,

  -- esta es la clave foránea única para la relación 1→1
  idVehiculo BIGINT NOT NULL UNIQUE,

  CONSTRAINT fk_seguro_vehiculo FOREIGN KEY (idVehiculo)
    REFERENCES vehiculo(id)
    ON DELETE CASCADE
    ON UPDATE CASCADE
) ENGINE = InnoDB;
