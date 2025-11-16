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
import java.util.regex.Pattern; 

/**
 * Controlador de las operaciones del menu (Menu Handler).
 * Maneja la interacción con el usuario y aplica validaciones de entrada inmediatas
 * con ciclo cerrado (repite la pregunta hasta que el dato es valido), incluyendo la
 * verificacion de unicidad del Dominio.
 */
public class MenuHandler {
    
    private final Scanner scanner;
    private final VehiculoServiceImpl vehiculoService;
    private final SeguroVehicularServiceImpl seguroService;

    // PATRON DE REGEX PARA EL FORMATO DE DOMINIO (LLNNNLL)
    private static final String PATRON_DOMINIO = "^[A-Z]{2}[0-9]{3}[A-Z]{2}$";
    private static final Pattern PATTERN = Pattern.compile(PATRON_DOMINIO);
    
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
            
            // 🚨 CAMBIO CRÍTICO: LECTURA, VALIDACION DE FORMATO Y UNICIDAD INMEDIATA
            String dominio = leerDominioYValidarUnicidad("Dominio (Patente LLNNNLL, ej. AB123CD): "); 
            
            String marca = leerString("Marca: ");
            String modelo = leerString("Modelo: ");
            int anio = leerInt("Ano (ej. 2024): ", 1950, LocalDate.now().getYear() + 1);
            String nroChasis = leerString("Nro. Chasis: ");
            
            Vehiculo vehiculo = new Vehiculo(0, false, dominio, marca, modelo, anio, nroChasis);
            
            System.out.println("--- Datos del Seguro (Requerido) ---");
            String aseguradora = leerString("Aseguradora: ");
            
            // NOTA: La unicidad de Nro. Poliza se deja en la capa Service/DAO
            // ya que suele requerir una conexión transaccional más compleja
            // y no es la clave de búsqueda principal como el Dominio.
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
            imprimirVehiculoFormatoCuadro(v);
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
            
            // Lógica de actualización de año con validación (ciclo cerrado)
            int nuevoAnio = v.getAnio();
            String nuevoAnioStr = leerStringOpcional("Nuevo Ano (Dejar vacio para no cambiar): ");
            if (!nuevoAnioStr.isEmpty()) {
                try {
                    nuevoAnio = Integer.parseInt(nuevoAnioStr);
                    if (nuevoAnio < 1950 || nuevoAnio > LocalDate.now().getYear() + 1) {
                         throw new NumberFormatException("El ano es invalido.");
                    }
                    v.setAnio(nuevoAnio);
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: El año ingresado no es un número entero válido o está fuera de rango (1950-" + (LocalDate.now().getYear() + 1) + ").");
                    return; 
                }
            }
            
            if (!nuevoModelo.isEmpty()) v.setModelo(nuevoModelo);
            
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
            if (v.getSeguro() != null) {
                 System.err.println("Error: El vehiculo con ID " + idVehiculo + " ya tiene un seguro asociado (Poliza " + v.getSeguro().getNroPoliza() + ").");
                 return;
            }
            
            String aseguradora = leerString("Aseguradora: ");
            String nroPoliza = leerString("Nro. Poliza: ");
            Cobertura cobertura = leerCobertura();
            LocalDate vencimiento = leerFecha("Fecha Vencimiento (YYYY-MM-DD): ");
            
            SeguroVehicular seguro = new SeguroVehicular(0, false, aseguradora, nroPoliza, cobertura, vencimiento);
            
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
            
            LocalDate nuevaFecha = leerFechaOpcional("Nueva Fecha Vencimiento YYYY-MM-DD (Dejar vacio para no cambiar): ");
            
            if (!nuevaPoliza.isEmpty()) s.setNroPoliza(nuevaPoliza);
            if (nuevaFecha != null) s.setVencimiento(nuevaFecha);
            
            seguroService.actualizar(s);
            System.out.println("EXITO: Seguro actualizado.");
            
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
        // Solo validamos el formato, ya que no estamos insertando
        String dominio = leerDominio("Ingrese Dominio (Patente LLNNNLL): ");
        
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
    // MÉTODOS AUXILIARES CON CICLO CERRADO Y VALIDACIÓN DE FORMATO/UNICIDAD
    // =================================================================

    /**
     * Valida el formato del dominio y lo limpia (mayúsculas y sin espacios/guiones).
     * @return El dominio limpio y validado.
     */
    private String validarDominioFormato(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("El dominio no puede estar vacio.");
        }
        
        // Limpiar: Mayusculas y quitar espacios y guiones
        String dominioLimpio = input.trim().toUpperCase().replace(" ", "").replace("-", "");

        if (!PATTERN.matcher(dominioLimpio).matches()) {
            throw new IllegalArgumentException("El formato del dominio es incorrecto. Debe ser LL NNN LL (ej. AB 123 CD).");
        }
        
        return dominioLimpio;
    }
    
    /**
     * Lee la entrada del usuario para el Dominio, aplicando validación inmediata (ciclo cerrado).
     * Se usa para búsquedas donde solo importa el formato.
     */
    private String leerDominio(String mensaje) {
        String input;
        while (true) {
            System.out.print(mensaje);
            input = scanner.nextLine().trim();
            try {
                return validarDominioFormato(input);
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Lee la entrada del usuario para el Dominio, valida formato y unicidad (ciclo cerrado).
     * Se usa para la creación de nuevos vehículos.
     */
    private String leerDominioYValidarUnicidad(String mensaje) {
        String input;
        while (true) {
            System.out.print(mensaje);
            input = scanner.nextLine().trim();
            String dominioLimpio;
            try {
                // 1. Validar el formato (REGEX)
                dominioLimpio = validarDominioFormato(input);
                
                // 2. Validar Unicidad (Regla de Negocio)
                if (vehiculoService.buscarPorDominio(dominioLimpio) != null) {
                    throw new IllegalArgumentException("ERROR DE UNICIDAD: Ya existe un vehiculo activo con la patente " + dominioLimpio + ".");
                }
                
                return dominioLimpio;
                
            } catch (IllegalArgumentException e) {
                System.err.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error al verificar unicidad en la BD: " + e.getMessage());
                // Si hay un error de conexión o BD, se recomienda abortar o reintentar
                return ""; // Devuelve vacío para forzar la reentrada o abortar
            }
        }
    }

    /**
     * Lee un String obligatorio, repite si está vacío (ciclo cerrado).
     */
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
    
    /**
     * Lee un String opcional (puede estar vacío).
     */
    private String leerStringOpcional(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    /**
     * Lee un entero, valida que sea un número y que esté en el rango (ciclo cerrado).
     */
    private int leerInt(String mensaje, int min, int max) {
        while (true) {
            try {
                System.out.print(mensaje);
                String inputStr = scanner.nextLine().trim();
                
                if (inputStr.isEmpty()) {
                    System.err.println("Error: El campo no puede estar vacio.");
                    continue; // Vuelve al inicio del while
                }
                
                int input = Integer.parseInt(inputStr);

                if (input < min || input > max) {
                    System.err.println("Error: El numero debe estar entre " + min + " y " + max + ".");
                } else {
                    return input; // Sale del ciclo
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Debe ingresar un numero entero valido.");
            }
        }
    }

    /**
     * Lee una Cobertura (Enum), repite hasta que sea una opción válida (ciclo cerrado).
     */
    private Cobertura leerCobertura() {
        while (true) {
            System.out.print("Cobertura (RC, TERCEROS, TODO_RIESGO): ");
            try {
                String input = scanner.nextLine().trim().toUpperCase();
                return Cobertura.valueOf(input); // Valida que el String corresponda al Enum
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Valor no valido. Use una de las opciones.");
            }
        }
    }

    /**
     * Lee una fecha, valida el formato y que no sea anterior a hoy (ciclo cerrado).
     */
    private LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.err.println("Error: La fecha no puede estar vacia.");
                    continue;
                }
                
                LocalDate fecha = LocalDate.parse(input);
                
                if (fecha.isBefore(LocalDate.now())) {
                    System.err.println("Error: La fecha no puede ser anterior a hoy.");
                } else {
                    return fecha; // Sale del ciclo
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Formato de fecha invalido. Use YYYY-MM-DD.");
            }
        }
    }
    
    /**
     * Lee una fecha OPCIONAL, valida el formato si se ingresa un valor.
     * @return La fecha si es válida, o null si el campo se dejó vacío.
     */
    private LocalDate leerFechaOpcional(String mensaje) {
         while (true) {
            System.out.print(mensaje);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return null; // Acepta el valor vacío y sale
            }
            try {
                LocalDate fecha = LocalDate.parse(input);
                
                if (fecha.isBefore(LocalDate.now())) {
                    System.err.println("Error: La fecha no puede ser anterior a hoy.");
                } else {
                    return fecha; // Sale del ciclo
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error: Formato de fecha invalido. Use YYYY-MM-DD.");
            }
        }
    }
    
    // =================================================================
    // MÉTODOS DE IMPRESIÓN Y UTILIDAD
    // =================================================================
    
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