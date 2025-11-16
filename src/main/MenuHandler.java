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

public class MenuHandler {
    
    private final Scanner scanner;
    private final VehiculoServiceImpl vehiculoService;
    private final SeguroVehicularServiceImpl seguroService;

    public MenuHandler(Scanner scanner, VehiculoServiceImpl vehiculoService, SeguroVehicularServiceImpl seguroService) {
        this.scanner = scanner;
        this.vehiculoService = vehiculoService;
        this.seguroService = seguroService;
    }

    // =================================================================
    // MÉTODOS DE INTERACCION (Llamados por AppMenu)
    // =================================================================

    // --- CRUD VEHICULO (A + B) ---

    public void crearVehiculoConSeguro() {
        try {
            System.out.println("\n--- 1. Crear Vehiculo (Transaccional) ---");
            String dominio = leerString("Dominio (Patente): ");
            String marca = leerString("Marca: ");
            String modelo = leerString("Modelo: ");
            int anio = leerInt("Ano (ej. 2024): ", 1950, LocalDate.now().getYear() + 1);
            String nroChasis = leerString("Nro. Chasis: ");
            
            Vehiculo vehiculo = new Vehiculo(0, false, dominio, marca, modelo, anio, nroChasis);
            
            System.out.println("--- Datos del Seguro (Requerido) ---");
            String aseguradora = leerString("Aseguradora: ");
            String nroPoliza = leerString("Nro. Poliza: ");
            Cobertura cobertura = leerCobertura();
            LocalDate vencimiento = leerFecha("Fecha Vencimiento (YYYY-MM-DD): ");
            
            SeguroVehicular seguro = new SeguroVehicular(0, false, aseguradora, nroPoliza, cobertura, vencimiento);
            
            vehiculo.setSeguro(seguro);
            
            vehiculoService.insertar(vehiculo);
            
            System.out.println("-----------------------------------------------------");
            System.out.println("EXITO: Vehiculo y Seguro creados (Transaccion OK).");
            System.out.println("ID Vehiculo: " + vehiculo.getId() + " | ID Seguro: " + vehiculo.getSeguro().getId());
            System.out.println("-----------------------------------------------------");

        } catch (Exception e) {
            System.err.println("\nERROR AL CREAR (Rollback ejecutado): " + e.getMessage());
        }
    }

    public void listarVehiculos() throws Exception {
        System.out.println("\n--- 2. Listar Vehiculos (con Seguros) ---");
        List<Vehiculo> vehiculos = vehiculoService.getAll();
        
        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehiculos activos en el sistema.");
            return;
        }
        
        for (Vehiculo v : vehiculos) {
            System.out.println(v.toString());
            System.out.println("--------------------");
        }
    }

    public void buscarVehiculoPorId() throws Exception {
        System.out.println("\n--- 3. Buscar Vehiculo por ID ---");
        int id = leerInt("Ingrese ID del Vehiculo: ", 1, Integer.MAX_VALUE);
        
        Vehiculo v = vehiculoService.getById(id);
        
        if (v == null) {
            System.err.println("Vehiculo con ID " + id + " no encontrado o esta eliminado.");
            return;
        }
        
        System.out.println("\nVehiculo encontrado:");
        imprimirVehiculoFormatoCuadro(v);
    }
    
    public void actualizarVehiculo() {
         System.out.println("\n--- 4. Actualizar Vehiculo (Transaccional) ---");
         try {
            int id = leerInt("Ingrese ID del Vehiculo a actualizar: ", 1, Integer.MAX_VALUE);
            Vehiculo v = vehiculoService.getById(id);
            
            if (v == null) {
                System.err.println("Vehiculo no encontrado.");
                return;
            }
            
            System.out.println("Datos actuales: Modelo=" + v.getModelo() + ", Ano=" + v.getAnio());
            String nuevoModelo = leerStringOpcional("Nuevo Modelo (Dejar vacio para no cambiar): ");
            String nuevoAnioStr = leerStringOpcional("Nuevo Ano (Dejar vacio para no cambiar): ");
            
            if (!nuevoModelo.isEmpty()) v.setModelo(nuevoModelo);
            if (!nuevoAnioStr.isEmpty()) v.setAnio(Integer.parseInt(nuevoAnioStr));
            
            if (v.getSeguro() != null) {
                System.out.println("Datos actuales Seguro: Poliza=" + v.getSeguro().getNroPoliza());
                String nuevaPoliza = leerStringOpcional("Nueva Poliza (Dejar vacio para no cambiar): ");
                if (!nuevaPoliza.isEmpty()) v.getSeguro().setNroPoliza(nuevaPoliza);
            } else {
                 System.out.println("Este vehiculo no tiene un seguro activo para actualizar.");
            }
            
            vehiculoService.actualizar(v);
            System.out.println("EXITO: Vehiculo actualizado (Transaccion OK).");

         } catch (Exception e) {
             System.err.println("\nERROR AL ACTUALIZAR (Rollback ejecutado): " + e.getMessage());
         }
    }

    public void eliminarVehiculo() {
        System.out.println("\n--- 5. Eliminar Vehiculo (Baja Logica Tx) ---");
        try {
            int id = leerInt("Ingrese ID del Vehiculo a eliminar: ", 1, Integer.MAX_VALUE);
            
            vehiculoService.eliminar(id);
            
            System.out.println("EXITO: Vehiculo y su seguro asociado han sido dados de baja (Transaccion OK).");
            
        } catch (Exception e) {
            System.err.println("\nERROR AL ELIMINAR (Rollback ejecutado): " + e.getMessage());
        }
    }

    // --- CRUD SEGURO (B) ---

    public void crearSeguroIndependiente() {
        try {
            System.out.println("\n--- 6. Crear Seguro (para Vehiculo existente) ---");
            int idVehiculo = leerInt("Ingrese el ID del Vehiculo al que pertenecera: ", 1, Integer.MAX_VALUE);
            
            Vehiculo v = vehiculoService.getById(idVehiculo);
            if (v == null) {
                 System.err.println("Error: No existe ningun vehiculo activo con ID: " + idVehiculo);
                 return;
            }
            // Validacion de unicidad 1:1
            if (v.getSeguro() != null) {
                 System.err.println("Error: El vehiculo con ID " + idVehiculo + " ya tiene un seguro asociado (Poliza " + v.getSeguro().getNroPoliza() + ").");
                 return;
            }
            
            String aseguradora = leerString("Aseguradora: ");
            String nroPoliza = leerString("Nro. Poliza: ");
            Cobertura cobertura = leerCobertura();
            LocalDate vencimiento = leerFecha("Fecha Vencimiento (YYYY-MM-DD): ");
            
            SeguroVehicular seguro = new SeguroVehicular(0, false, aseguradora, nroPoliza, cobertura, vencimiento);
            
            // LLamada al service con la FK
            seguroService.insertar(seguro, idVehiculo);
            
            System.out.println("EXITO: Seguro independiente creado con ID: " + seguro.getId());

        } catch (Exception e) {
            System.err.println("\nERROR AL CREAR SEGURO: " + e.getMessage());
        }
    }
    
    public void actualizarSeguroIndependiente() {
        System.out.println("\n--- 7. Actualizar Seguro por ID ---");
        try {
            int id = leerInt("Ingrese ID del Seguro a actualizar: ", 1, Integer.MAX_VALUE);
            SeguroVehicular s = seguroService.getById(id);
            
            if (s == null) {
                System.err.println("Seguro con ID " + id + " no encontrado o esta eliminado.");
                return;
            }
            
            System.out.println("Datos actuales: Poliza=" + s.getNroPoliza() + ", Vencimiento=" + s.getVencimiento());
            
            String nuevaPoliza = leerStringOpcional("Nueva Poliza (Dejar vacio para no cambiar): ");
            String nuevaFechaStr = leerStringOpcional("Nueva Fecha Vencimiento YYYY-MM-DD (Dejar vacio para no cambiar): ");
            
            if (!nuevaPoliza.isEmpty()) s.setNroPoliza(nuevaPoliza);
            if (!nuevaFechaStr.isEmpty()) s.setVencimiento(LocalDate.parse(nuevaFechaStr));
            
            seguroService.actualizar(s);
            System.out.println("EXITO: Seguro actualizado.");
            
        } catch (DateTimeParseException e) {
            System.err.println("\nERROR: Formato de fecha invalido. Use YYYY-MM-DD.");
        } catch (Exception e) {
            System.err.println("\nERROR AL ACTUALIZAR SEGURO: " + e.getMessage());
        }
    }
    
    public void eliminarSeguroIndependiente() {
        System.out.println("\n--- 8. Eliminar Seguro por ID (Baja Logica) ---");
        try {
            int id = leerInt("Ingrese ID del Seguro a eliminar: ", 1, Integer.MAX_VALUE);
            
            seguroService.eliminar(id);
            
            System.out.println("EXITO: Seguro con ID " + id + " dado de baja logica.");
            
        } catch (Exception e) {
            System.err.println("\nERROR AL ELIMINAR SEGURO: " + e.getMessage());
        }
    }

    public void listarSeguros() throws Exception {
         System.out.println("\n--- 9. Listar Seguros ---");
         
        List<SeguroVehicular> seguros = seguroService.getAll();
        
        if (seguros.isEmpty()) {
            System.out.println("No hay seguros activos en el sistema.");
            return;
        }
        
        for (SeguroVehicular s : seguros) {
            System.out.println(s.toString());
            System.out.println("--------------------");
        }
    }

    // --- BÚSQUEDAS POR CAMPO CLAVE ---

    public void buscarVehiculoPorDominio() throws Exception {
        System.out.println("\n--- 10. Buscar Vehiculo por Dominio ---");
        String dominio = leerString("Ingrese Dominio (Patente): ");
        
        Vehiculo v = vehiculoService.buscarPorDominio(dominio);
        
        if (v == null) {
            System.err.println("No se encontro vehiculo activo con el dominio: " + dominio);
            return;
        }
        
        System.out.println("\nVehiculo encontrado:");
        imprimirVehiculoFormatoCuadro(v);
    }

    public void buscarSeguroPorPoliza() throws Exception {
        System.out.println("\n--- 11. Buscar Seguro por Nro. Poliza ---");
        String poliza = leerString("Ingrese Nro. Poliza: ");
        
        SeguroVehicular s = seguroService.buscarPorPoliza(poliza);
        
        if (s == null) {
            System.err.println("No se encontro seguro activo con la poliza: " + poliza);
            return;
        }
        
        System.out.println("\nSeguro encontrado:");
        System.out.println(s.toString());
    }

    // =================================================================
    // MÉTODOS AUXILIARES
    // =================================================================

    private String leerString(String mensaje) {
        String input;
        while (true) {
            System.out.print(mensaje);
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.err.println("Error: El campo no puede estar vacio.");
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
                scanner.nextLine(); // Consumir salto de linea
                if (input < min || input > max) {
                    System.err.println("Error: El numero debe estar entre " + min + " y " + max + ".");
                } else {
                    return input;
                }
            } catch (InputMismatchException e) {
                System.err.println("Error: Debe ingresar un numero entero valido.");
                scanner.nextLine(); // Limpiar buffer
            }
        }
    }

    private Cobertura leerCobertura() {
        while (true) {
            System.out.print("Cobertura (RC, TERCEROS, TODO_RIESGO): ");
            try {
                String input = scanner.nextLine().trim().toUpperCase();
                return Cobertura.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Valor no valido. Use una de las opciones.");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                String input = scanner.nextLine().trim();
                LocalDate fecha = LocalDate.parse(input);
                
                if (fecha.isBefore(LocalDate.now())) {
                    System.err.println("Error: La fecha no puede ser anterior a hoy.");
                } else {
                    return fecha;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Formato de fecha invalido. Use YYYY-MM-DD.");
            }
        }
    }
    
    private void imprimirVehiculoFormatoCuadro(Vehiculo v) {
        System.out.println("----------------------------------------");
        System.out.println("|    DETALLES DEL VEHICULO (A)         |");
        System.out.println("----------------------------------------");
        System.out.println("| ID Vehiculo: " + v.getId());
        System.out.println("| Dominio:     " + v.getDominio());
        System.out.println("| Marca:       " + v.getMarca());
        System.out.println("| Modelo:      " + v.getModelo());
        System.out.println("| Ano:         " + v.getAnio());
        System.out.println("| Nro. Chasis: " + v.getNroChasis());
        System.out.println("| Eliminado:   " + v.isEliminado());
        System.out.println("----------------------------------------");
        
        if (v.getSeguro() != null) {
            SeguroVehicular s = v.getSeguro();
            System.out.println("|    DETALLES DEL SEGURO (B)           |");
            System.out.println("----------------------------------------");
            System.out.println("| ID Seguro:   " + s.getId());
            System.out.println("| Aseguradora: " + s.getAseguradora());
            System.out.println("| Nro. Poliza: " + s.getNroPoliza());
            System.out.println("| Cobertura:   " + s.getCobertura());
            System.out.println("| Vencimiento: " + s.getVencimiento());
        } else {
            System.out.println("|    SEGURO (B): Sin seguro asociado   |");
        }
        System.out.println("----------------------------------------");
    }

    public void pausarParaContinuar() {
        System.out.println("\nPresione [Enter] para volver al menu...");
        scanner.nextLine();
    }
}