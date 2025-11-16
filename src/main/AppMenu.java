package main;

import dao.SeguroVehicularDAO;
import dao.VehiculoDAO;
import service.SeguroVehicularServiceImpl;
import service.VehiculoServiceImpl;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Orquestador principal del menu de la aplicacion (Punto de entrada).
 */
public class AppMenu {

    private final Scanner scanner;
    private final MenuHandler menuHandler;
    // 'running' ya no es necesario, el loop se controla con 'opcion != 0'

    public AppMenu() {
        this.scanner = new Scanner(System.in);
        
        // --- INYECCIÓN DE DEPENDENCIAS (Configuración de capas) ---
        SeguroVehicularDAO seguroDAO = new SeguroVehicularDAO();
        VehiculoDAO vehiculoDAO = new VehiculoDAO(seguroDAO);
        
        // El Service de Seguro se necesita para el Service de Vehiculo
        SeguroVehicularServiceImpl seguroService = new SeguroVehicularServiceImpl(seguroDAO);
        VehiculoServiceImpl vehiculoService = new VehiculoServiceImpl(vehiculoDAO, seguroService);
        
        // El Handler necesita el Scanner y los Services para operar
        this.menuHandler = new MenuHandler(scanner, vehiculoService, seguroService);
        // --- FIN INYECCIÓN ---
    }

    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    /**
     * Ciclo principal del menu.
     */
    public void run() {
        int opcion;
        do {
            MenuDisplay.mostrarMenuPrincipal();
            opcion = readOption();
            
            if (opcion != 0) {
                processOption(opcion);
                if (opcion >= 1 && opcion <= 11) { // Pausar solo si se ejecuta una opción válida
                    menuHandler.pausarParaContinuar(); 
                }
            }
            
        } while (opcion != 0);
        
        System.out.println("\nCerrando la aplicacion. Hasta luego!");
    }
    
    /**
     * Lee la opción del usuario con manejo básico de errores de tipo.
     * @return La opción elegida o -1 si hubo un error.
     */
    private int readOption() {
        try {
            int opcion = scanner.nextInt();
            scanner.nextLine(); // Consumir el salto de linea
            return opcion;
        } catch (InputMismatchException e) {
            System.err.println("Error: Ingrese un numero valido.");
            scanner.nextLine(); // Limpiar el buffer
            return -1;
        }
    }

    /**
     * Ejecuta la lógica correspondiente a la opción seleccionada.
     */
    private void processOption(int opcion) {
        try {
            switch (opcion) {
                // VEHICULOS (CRUD Compuesto A + B)
                case 1:
                    menuHandler.crearVehiculoConSeguro();
                    break;
                case 2:
                    menuHandler.listarVehiculos();
                    break;
                case 3:
                    menuHandler.buscarVehiculoPorId();
                    break;
                case 4:
                    menuHandler.actualizarVehiculo();
                    break;
                case 5:
                    menuHandler.eliminarVehiculo();
                    break;
                // SEGUROS (CRUD Individual B)
                case 6: // ✅ NUEVA POSICIÓN
                    menuHandler.crearSeguroIndependiente(); 
                    break;
                case 7: // ✅ NUEVA POSICIÓN
                    menuHandler.actualizarSeguroIndependiente(); 
                    break;
                case 8: // ✅ NUEVA POSICIÓN
                    menuHandler.eliminarSeguroIndependiente(); 
                    break;
                case 9: // ✅ NUEVA POSICIÓN
                    menuHandler.listarSeguros(); 
                    break;
                // BÚSQUEDAS POR CAMPO CLAVE
                case 10: // ✅ NUEVA OPCIÓN
                    menuHandler.buscarVehiculoPorDominio();
                    break;
                case 11: // ✅ NUEVA OPCIÓN
                    menuHandler.buscarSeguroPorPoliza();
                    break;
                case 0:
                    // La logica de salida esta en el loop run()
                    break;
                default:
                    System.err.println("Opcion no valida. Intente de nuevo.");
            }
        } catch (Exception e) {
            // Captura generica de errores de la capa Service (Rollbacks)
            System.err.println("\n!!! ERROR INESPERADO (CAPA MAIN): " + e.getMessage());
            // e.printStackTrace(); // Descomentar para debug
        }
    }
}