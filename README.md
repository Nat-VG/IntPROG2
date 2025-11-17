# TPI Programación II – Gestión de Vehículos y Seguros

Sistema de consola (Java + MySQL) que gestiona vehículos (Entidad A) y seguros vehiculares (Entidad B) en una relación 1–1 con operaciones transaccionales compuestas (crear / actualizar / eliminar A junto con B) y CRUD independiente para B. Incluye validaciones de negocio, unicidad lógica, bajas lógicas y búsquedas por campo clave.

## 1. Prerrequisitos
- Java JDK 8+ (recomendado 11 o superior)
- NetBeans (desarrollo y ejecución principal)
- MySQL Server (puerto default 3306)
- DBeaver (o cualquier cliente SQL) para ejecutar el script inicial
- Conector JDBC MySQL 8.x (incluido el JAR `mysql-connector-java-8.0.30.jar` en la raíz del repo)

## 2. Clonado del Proyecto
Clonar el repositorio (ya realizado en tu entorno):
```
git clone https://github.com/Nat-VG/IntPROG2.git
```
Abrir el proyecto en NetBeans: *File > Open Project* y seleccionar la carpeta `IntPROG2`.

## 3. Preparación de la Base de Datos
1. Abrir DBeaver (o cliente MySQL).
2. Crear conexión hacia el servidor (usuario `root`).
3. Ejecutar el script completo de inicialización:
	 - Archivo: `sql/Script_unico.sql`
	 - Este script:
		 - Borra DB previa (`DROP DATABASE IF EXISTS BaseVehiculos;`)
		 - Crea la DB `BaseVehiculos`
		 - Crea tablas `vehiculo` y `segurovehicular`
		 - Define `UNIQUE` en `dominio`, `nroPoliza` y `idVehiculo`
		 - Inserta datos de prueba

## 4. Configuración de Conexión
Editar `src/config/DatabaseConnection.java` y establecer la contraseña MySQL:
Si usas otro usuario/puerto, actualiza también `URL` y `USER`.

## 5. Añadir el Driver JDBC (si NetBeans no lo detecta)
En NetBeans:
1. Click derecho sobre el proyecto > *Properties* > *Libraries*.
2. Añadir JAR/Folder.
3. Seleccionar `mysql-connector-java-8.0.30.jar`.

## 6. Ejecución
Método recomendado: desde NetBeans.
1. Asegúrate de haber configurado la contraseña en `DatabaseConnection`.
2. Run Project o ejecutar la clase `main.AppMenu` (tiene el método `main`).
3. Se mostrará el menú interactivo en la consola.

## 7. Menú y Funcionalidades
CRUD Compuesto (Vehículo + Seguro):
1. Crear Vehículo (opcional seguro) – transacción A+B
2. Listar Vehículos (incluye seguro si existe)
3. Buscar Vehículo por ID
4. Actualizar Vehículo (y su seguro si existe)
5. Eliminar Vehículo (baja lógica A y B)

CRUD Independiente Seguro (Entidad B):
6. Crear Seguro para Vehículo existente
7. Actualizar Seguro
8. Eliminar Seguro (baja lógica)
9. Listar Seguros

Búsquedas por campo clave:
10. Buscar Vehículo por Dominio (patente)
11. Buscar Seguro por Número de Póliza
0. Salir

## 8. Arquitectura (Capas)
- `entities/`: Modelos (`Vehiculo`, `SeguroVehicular`, `Cobertura`, `Base`).
- `dao/`: Acceso a datos (`VehiculoDAO`, `SeguroVehicularDAO`, `GenericDAO`). JOIN y mapeo manual.
- `service/`: Lógica de negocio y orquestación transaccional (`VehiculoServiceImpl`, `SeguroVehicularServiceImpl`, `GenericService`). Validaciones y unicidad.
- `config/`: Conexión y `TransactionManager` controlando `commit/rollback`.
- `main/`: `AppMenu`, `MenuHandler`, `MenuDisplay` (interfaz consola).
- `sql/`: Script único para inicialización.

## 9. Validaciones Clave
Vehículo:
- Dominio obligatorio y único (normalizado a mayúsculas).
- Marca / Modelo obligatorios.
- Año en rango [1950 .. añoActual+1].
- Número de chasis obligatorio.

Seguro:
- Aseguradora / Número de póliza obligatorios y póliza única.
- Vencimiento debe ser futuro.
- Cobertura pertenece al enum (`RC`, `TERCEROS`, `TODO_RIESGO`).

Bajas lógicas: campo booleano `eliminado` (no se borra físicamente).

## 10. Transacciones
- Crear/Actualizar/Eliminar Vehículo: se ejecuta en una única transacción que incluye su seguro.
- Crear/Actualizar/Eliminar Seguro independiente: transacción aislada.
- `TransactionManager` fuerza `AutoCommit=false`, hace `commit()` o `rollback()` y restablece el estado.

## 11. Script SQL
Archivo: `sql/Script_unico.sql` (idempotente: borra, crea y pobla). Ejecutar siempre antes de primera corrida.

## 12. Errores Comunes
- Driver no cargado: verificar el JAR agregado.
- Credenciales incorrectas: ajustar usuario/contraseña en `DatabaseConnection`.
- Puertos distintos: modificar URL JDBC.
- Colisión de dominio/póliza: mensaje de unicidad desde capa Service.

## 13. Dependencias
- MySQL Connector J 8.0.30 (incluido).

## 14. Ejemplo Rápido (Crear Vehículo con Seguro)
Ingresar opción 1 y seguir los prompts:
```
Dominio: AA123BB
Marca: Toyota
Modelo: Corolla
Año: 2021
Nro Chasis: CHS-TY-999
¿Agregar seguro? (S/N): S
Aseguradora: Sancor
Nro Póliza: POL999
Cobertura (RC/TERCEROS/TODO_RIESGO): RC
Vencimiento (YYYY-MM-DD): 2026-05-01
```

## 15. Licencia / Uso Académico
Proyecto educativo para el Trabajo Práctico Integrador de Programación II.
