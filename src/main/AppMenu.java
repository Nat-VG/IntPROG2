package main;

// Importamos las clases de las otras capas
import dao.SeguroVehicularDAO;
import dao.VehiculoDAO;
import service.SeguroVehicularServiceImpl;
import service.VehiculoServiceImpl;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Orquestador principal del menú de la aplicación (Punto de entrada).
 * Responsabilidades:
 * 1. Inicializar la cadena de dependencias (DI manual).
 * 2. Ejecutar el loop principal del menú.
 * 3. Delegar la lógica de interacción a MenuHandler.
 * Adaptado del ejemplo AppMenu.java del TPI.
 *
 * @author [Tu Nombre/Grupo Aquí]
 */
public class AppMenu {

    private final Scanner scanner;
    private final MenuHandler menuHandler;
    private boolean running;

    /**
     * Constructor que inicializa la aplicación y la cadena de dependencias (DI).
     * Aquí se "enciende" la aplicación y se ensamblan todas las capas.
     */
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        
        // =================================================================
        // INYECCIÓN DE DEPENDENCIAS (DI) - Ensamblado de la aplicación
        // =================================================================
        
        // 1. Crear DAOs (Capa más baja)
        // El DAO de Seguro (B) no depende de nadie
        SeguroVehicularDAO seguroDAO = new SeguroVehicularDAO();
        // El DAO de Vehículo (A) depende del DAO de Seguro (B)
        VehiculoDAO vehiculoDAO = new VehiculoDAO(seguroDAO);

        // 2. Crear Services (Capa de negocio)
        // El Service de Seguro (B) depende de su DAO (B)
        SeguroVehicularServiceImpl seguroService = new SeguroVehicularServiceImpl(seguroDAO);
        // El Service de Vehículo (A) depende de su DAO (A) y del Service de B
        VehiculoServiceImpl vehiculoService = new VehiculoServiceImpl(vehiculoDAO, seguroService);
        
        // 3. Crear el Handler (Capa de presentación)
        // El Handler necesita ambos services para operar (A y B)
        this.menuHandler = new MenuHandler(scanner, vehiculoService, seguroService);
        
        // =================================================================
        
        this.running = true;
    }

    /**
     * Punto de entrada de la aplicación (main).
     * @param args Argumentos de línea de comandos (no usados).
     */
    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    /**
     * Loop principal del menú.
     * Se ejecuta hasta que el usuario elija la opción 0.
     */
    public void run() {
        while (running) {
            MenuDisplay.mostrarMenuPrincipal();
            try {
                int opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de línea (CRÍTICO)
                processOption(opcion);
            } catch (InputMismatchException e) {
                System.err.println("Error: Debe ingresar un número.");
                scanner.nextLine(); // Limpiar buffer del scanner
            }
        }
        System.out.println("Saliendo de la aplicación...");
        scanner.close();
    }

    /**
     * Switch principal que delega la acción al MenuHandler.
     * @param opcion La opción seleccionada por el usuario.
     */
    private void processOption(int opcion) {
        try {
            switch (opcion) {
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
                case 6:
                    menuHandler.buscarVehiculoPorDominio();
                    break;
                case 7:
                    menuHandler.buscarSeguroPorPoliza();
                    break;
                case 8:
                    menuHandler.crearSeguroIndependiente();
                    break;
                case 9:
                    menuHandler.listarSeguros();
                    break;
                case 0:
                    running = false;
                    break;
                default:
                    System.err.println("Opción no válida. Intente de nuevo.");
            }
        } catch (Exception e) {
            // Captura genérica de errores de la capa Service (Rollbacks)
            System.err.println("\n!!! ERROR INESPERADO (CAPA MAIN): " + e.getMessage());
            // e.printStackTrace(); // Descomentar para debug
        }
    }
    

}