package main;

import entities.Vehiculo;
import entities.SeguroVehicular;
import entities.Cobertura;
import service.VehiculoServiceImpl;
import service.SeguroVehicularServiceImpl;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Controlador de las operaciones del menú (Menu Handler).
 * CORREGIDO: Añade una pausa (presione Enter) después de las
 * operaciones de listado y búsqueda para mejorar la legibilidad.
 */
public class MenuHandler {
    
    private final Scanner scanner;
    private final VehiculoServiceImpl vehiculoService;
    private final SeguroVehicularServiceImpl seguroService;

    /**
     * Constructor con Inyección de Dependencias.
     * @param scanner El Scanner global (desde AppMenu).
     * @param vehiculoService El Service principal (A).
     * @param seguroService El Service secundario (B).
     */
    public MenuHandler(Scanner scanner, VehiculoServiceImpl vehiculoService, SeguroVehicularServiceImpl seguroService) {
        this.scanner = scanner;
        this.vehiculoService = vehiculoService;
        this.seguroService = seguroService;
    }

    // =================================================================
    // MÉTODOS DE INTERACCIÓN (Llamados por AppMenu)
    // =================================================================

    /**
     * Opción 1: Crea Vehiculo y Seguro (Transacción Atómica 1:1).
     */
    public void crearVehiculoConSeguro() {
        try {
            System.out.println("\n--- 1. Crear Vehículo (Transaccional) ---");
            // ... (Lógica de carga de datos A y B) ...
            String dominio = leerString("Dominio (Patente): ");
            String marca = leerString("Marca: ");
            String modelo = leerString("Modelo: ");
            int anio = leerInt("Año (ej. 2024): ", 1950, LocalDate.now().getYear() + 1);
            String nroChasis = leerString("Nro. Chasis: ");
            Vehiculo vehiculo = new Vehiculo(0, false, dominio, marca, modelo, anio, nroChasis);
            
            System.out.println("--- Datos del Seguro (Requerido) ---");
            String aseguradora = leerString("Aseguradora: ");
            String nroPoliza = leerString("Nro. Póliza: ");
            Cobertura cobertura = leerCobertura();
            LocalDate vencimiento = leerFecha("Fecha Vencimiento (YYYY-MM-DD): ");
            SeguroVehicular seguro = new SeguroVehicular(0, false, aseguradora, nroPoliza, cobertura, vencimiento);
            
            vehiculo.setSeguro(seguro);
            
            // Llamada al Service (que maneja la Tx A->B)
            vehiculoService.insertar(vehiculo);
            
            System.out.println("-----------------------------------------------------");
            System.out.println("ÉXITO: Vehículo y Seguro creados (Transacción OK).");
            System.out.println("ID Vehículo: " + vehiculo.getId() + " | ID Seguro: " + vehiculo.getSeguro().getId());
            System.out.println("-----------------------------------------------------");

        } catch (Exception e) {
            System.err.println("\nERROR AL CREAR (Rollback ejecutado): " + e.getMessage());
        }
        
        // (No pausamos en crear, el mensaje de Éxito/Error es suficiente)
    }

    /**
     * Opción 2: Lista todos los vehículos (Demuestra Eager Loading 1:1).
     * @throws Exception Si falla la consulta al Service/DAO.
     */
    public void listarVehiculos() throws Exception {
        System.out.println("\n--- 2. Listar Vehículos (con Seguros) ---");
        List<Vehiculo> vehiculos = vehiculoService.getAll();
        
        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehículos activos en el sistema.");
        } else {
            for (Vehiculo v : vehiculos) {
                System.out.println(v.toString());
                System.out.println("--------------------");
            }
        }
        
        // AÑADIMOS LA PAUSA
        pausarParaContinuar();
    }

    /**
     * Opción 3: Buscar Vehículo por ID (Demuestra Eager Loading 1:1).
     * @throws Exception Si falla la consulta al Service/DAO.
     */
    public void buscarVehiculoPorId() throws Exception {
        System.out.println("\n--- 3. Buscar Vehículo por ID ---");
        int id = leerInt("Ingrese ID del Vehículo: ", 1, Integer.MAX_VALUE);
        
        Vehiculo v = vehiculoService.getById(id);
        
        if (v == null) {
            System.err.println("Vehículo con ID " + id + " no encontrado o está eliminado.");
        } else {
            System.out.println("\nVehículo encontrado:");
            imprimirVehiculoFormatoCuadro(v); // Usamos el formato de cuadro
        }
        
        // AÑADIMOS LA PAUSA
        pausarParaContinuar();
    }
    
    /**
     * Opción 4: Actualizar Vehículo (Demuestra Tx 1:1).
     */
    public void actualizarVehiculo() {
         System.out.println("\n--- 4. Actualizar Vehículo (Transaccional) ---");
         try {
            int id = leerInt("Ingrese ID del Vehículo a actualizar: ", 1, Integer.MAX_VALUE);
            Vehiculo v = vehiculoService.getById(id);
            
            if (v == null) {
                System.err.println("Vehículo no encontrado.");
                return; // Salimos (no pausamos si no se encontró)
            }
            
            // ... (Lógica de carga de datos para actualizar) ...
            System.out.println("Datos actuales: Modelo=" + v.getModelo() + ", Año=" + v.getAnio());
            String nuevoModelo = leerStringOpcional("Nuevo Modelo (Dejar vacío para no cambiar): ");
            // ... (etc) ...
            
            if (!nuevoModelo.isEmpty()) v.setModelo(nuevoModelo);
            // ...
            
            if (v.getSeguro() != null) {
                 String nuevaPoliza = leerStringOpcional("Nueva Póliza (Dejar vacío para no cambiar): ");
                 if (!nuevaPoliza.isEmpty()) v.getSeguro().setNroPoliza(nuevaPoliza);
            }
            
            vehiculoService.actualizar(v);
            System.out.println("ÉXITO: Vehículo actualizado (Transacción OK).");

         } catch (Exception e) {
             System.err.println("\nERROR AL ACTUALIZAR (Rollback ejecutado): " + e.getMessage());
         }
    }

    /**
     * Opción 5: Eliminar Vehículo (Baja Lógica Transaccional 1:1).
     */
    public void eliminarVehiculo() {
        System.out.println("\n--- 5. Eliminar Vehículo (Baja Lógica Tx) ---");
        try {
            int id = leerInt("Ingrese ID del Vehículo a eliminar: ", 1, Integer.MAX_VALUE);
            
            vehiculoService.eliminar(id);
            
            System.out.println("ÉXITO: Vehículo y su seguro asociado han sido dados de baja (Transacción OK).");
            
        } catch (Exception e) {
            System.err.println("\nERROR AL ELIMINAR (Rollback ejecutado): " + e.getMessage());
        }
    }

    /**
     * Opción 6: Búsqueda por Dominio (Requisito TPI).
     * @throws Exception Si falla la consulta al Service/DAO.
     */
    public void buscarVehiculoPorDominio() throws Exception {
        System.out.println("\n--- 6. Buscar Vehículo por Dominio ---");
        String dominio = leerString("Ingrese Dominio (Patente): ");
        
        Vehiculo v = vehiculoService.buscarPorDominio(dominio);
        
        if (v == null) {
            System.err.println("No se encontró vehículo activo con el dominio: " + dominio);
        } else {
            System.out.println("\nVehículo encontrado:");
            // Usamos el formato de cuadro aquí también para consistencia
            imprimirVehiculoFormatoCuadro(v);
        }
        
        // AÑADIMOS LA PAUSA
        pausarParaContinuar();
    }

    /**
     * Opción 7: Búsqueda por Póliza (Requisito TPI).
     * @throws Exception Si falla la consulta al Service/DAO.
     */
    public void buscarSeguroPorPoliza() throws Exception {
        System.out.println("\n--- 7. Buscar Seguro por Nro. Póliza ---");
        String poliza = leerString("Ingrese Nro. Póliza: ");
        
        SeguroVehicular s = seguroService.buscarPorPoliza(poliza);
        
        if (s == null) {
            System.err.println("No se encontró seguro activo con la póliza: " + poliza);
        } else {
            System.out.println("\nSeguro encontrado:");
            // (Podríamos hacer un "imprimirSeguroFormatoCuadro" similar, 
            // pero por ahora el toString() de Seguro es suficiente)
            System.out.println(s.toString());
        }
        
        // AÑADIMOS LA PAUSA
        pausarParaContinuar();
    }

    /**
     * Opción 8: Crear Seguro independiente (sin Tx 1:1).
     */
    public void crearSeguroIndependiente() {
        try {
            System.out.println("\n--- 8. Crear Seguro (Independiente) ---");
            System.out.println("NOTA: Para que esto funcione, la FK 'idVehiculo' en la BD debe ser NOT NULL.");
            int idVehiculo = leerInt("Ingrese el ID del Vehículo al que pertenecerá: ", 1, Integer.MAX_VALUE);
            
            // ... (Lógica de validación de Vehículo) ...
            Vehiculo v = vehiculoService.getById(idVehiculo);
            if (v == null) {
                 System.err.println("Error: No existe ningún vehículo activo con ID: " + idVehiculo);
                 return;
            }
            if (v.getSeguro() != null) {
                 System.err.println("Error: El vehículo con ID " + idVehiculo + " ya tiene un seguro asociado.");
                 return;
            }
            
            // ... (Lógica de carga de datos de Seguro) ...
            String aseguradora = leerString("Aseguradora: ");
            String nroPoliza = leerString("Nro. Póliza: ");
            Cobertura cobertura = leerCobertura();
            LocalDate vencimiento = leerFecha("Fecha Vencimiento (YYYY-MM-DD): ");
            
            SeguroVehicular seguro = new SeguroVehicular(0, false, aseguradora, nroPoliza, cobertura, vencimiento);
            seguro.setIdVehiculo(idVehiculo);
            
            seguroService.insertar(seguro);
            
            System.out.println("ÉXITO: Seguro independiente creado con ID: " + seguro.getId());

        } catch (Exception e) {
            System.err.println("\nERROR AL CREAR SEGURO: " + e.getMessage());
        }
    }

    /**
     * Opción 9: Listar Seguros independientes.
     * @throws Exception Si falla la consulta al Service/DAO.
     */
    public void listarSeguros() throws Exception {
         System.out.println("\n--- 9. Listar Seguros ---");
         
        List<SeguroVehicular> seguros = seguroService.getAll();
        
        if (seguros.isEmpty()) {
            System.out.println("No hay seguros activos en el sistema.");
        } else {
            for (SeguroVehicular s : seguros) {
                System.out.println(s.toString());
                System.out.println("--------------------");
            }
        }
        
        // AÑADIMOS LA PAUSA
        pausarParaContinuar();
    }

    // =================================================================
    // MÉTODOS AUXILIARES DE CAPTURA (Validación de entrada)
    // =================================================================

    private String leerString(String mensaje) {
        String input;
        while (true) {
            System.out.print(mensaje);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.err.println("Error: El campo no puede estar vacío.");
            } else {
                return input;
            }
        }
    }
    
    private String leerStringOpcional(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int leerInt(String mensaje, int min, int max) {
        while (true) {
            try {
                System.out.print(mensaje);
                int input = scanner.nextInt();
                scanner.nextLine(); // Consumir salto de línea
                if (input < min || input > max) {
                    System.err.println("Error: El número debe estar entre " + min + " y " + max + ".");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                System.err.println("Error: Debe ingresar un número entero válido.");
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }

    private Cobertura leerCobertura() {
        while (true) {
            System.out.print("Cobertura (RC, TERCEROS, TODO_RIESGO): ");
            try {
                String input = scanner.nextLine().trim().toUpperCase();
                return Cobertura.valueOf(input); // Convierte String a Enum
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Valor no válido. Use una de las opciones.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                String input = scanner.nextLine().trim();
                LocalDate fecha = LocalDate.parse(input); // Espera YYYY-MM-DD
                
                if (fecha.isBefore(LocalDate.now())) {
                    System.err.println("Error: La fecha no puede ser anterior a hoy.");
                } else {
                    return fecha;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Formato de fecha inválido. Use YYYY-MM-DD.");
            }
        }
    }
    
    // =================================================================
    // MÉTODOS AUXILIARES DE IMPRESIÓN (Añadidos por solicitud)
    // =================================================================

    /**
     * MÉTODO NUEVO: Imprime un vehículo con formato de cuadro.
     * @param v El Vehiculo a imprimir.
     */
    private void imprimirVehiculoFormatoCuadro(Vehiculo v) {
        System.out.println("----------------------------------------");
        System.out.println("|    DETALLES DEL VEHÍCULO (A)         |");
        System.out.println("----------------------------------------");
        System.out.println("| ID Vehículo: " + v.getId());
        System.out.println("| Dominio:     " + v.getDominio());
        System.out.println("| Marca:       " + v.getMarca());
        System.out.println("| Modelo:      " + v.getModelo());
        System.out.println("| Año:         " + v.getAnio());
        System.out.println("| Nro. Chasis: " + v.getNroChasis());
        System.out.println("| Eliminado:   " + v.isEliminado());
        System.out.println("----------------------------------------");
        
        if (v.getSeguro() != null) {
            SeguroVehicular s = v.getSeguro();
            System.out.println("|    DETALLES DEL SEGURO (B)           |");
            System.out.println("----------------------------------------");
            System.out.println("| ID Seguro:   " + s.getId());
            System.out.println("| Aseguradora: " + s.getAseguradora());
            System.out.println("| Nro. Póliza: " + s.getNroPoliza());
            System.out.println("| Cobertura:   " + s.getCobertura());
            System.out.println("| Vencimiento: " + s.getVencimiento());
            System.out.println("| ID Veh. (FK):" + s.getIdVehiculo());
        } else {
            System.out.println("|    SEGURO (B): Sin seguro asociado   |");
        }
        System.out.println("----------------------------------------");
    }

    /**
     * MÉTODO NUEVO: Pausa la ejecución hasta que el usuario presione Enter.
     */
    private void pausarParaContinuar() {
        System.out.println("\nPresione [Enter] para volver al menú...");
        scanner.nextLine();
    }
}