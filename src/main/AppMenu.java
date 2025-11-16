package main;

import dao.SeguroVehicularDAO;
import dao.VehiculoDAO;
import service.SeguroVehicularServiceImpl;
import service.VehiculoServiceImpl;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Orquestador principal del menu de la aplicacion (Punto de entrada).
 * CORREGIDO: 
 * 1. Mensajes de error sin acentos.
 * 2. El loop 'run()' ahora llama a 'pausarParaContinuar()' despues
 * de CADA operacion para mejorar la experiencia de usuario.
 */
public class AppMenu {

    private final Scanner scanner;
    private final MenuHandler menuHandler;
    // 'running' ya no es necesario, el loop se controla con 'opcion != 0'
    // private boolean running; // <-- ELIMINADO

    public AppMenu() {
        this.scanner = new Scanner(System.in);
        
        // --- INYECCIÓN DE DEPENDENCIAS ---
        SeguroVehicularDAO seguroDAO = new SeguroVehicularDAO();
        VehiculoDAO vehiculoDAO = new VehiculoDAO(seguroDAO);
        SeguroVehicularServiceImpl seguroService = new SeguroVehicularServiceImpl(seguroDAO);
        VehiculoServiceImpl vehiculoService = new VehiculoServiceImpl(vehiculoDAO, seguroService);
        this.menuHandler = new MenuHandler(scanner, vehiculoService, seguroService);
        // --- FIN INYECCIÓN ---
    }

    public static void main(String[] args) {
        AppMenu app = new AppMenu();
        app.run();
    }

    /**
     * Loop principal del menu, modificado para pausar despues de cada accion.
     */
    public void run() {
        int opcion = -1; // Inicializamos con un valor que no sea 0
        
        while (opcion != 0) {
            MenuDisplay.mostrarMenuPrincipal();
            try {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Consumir el salto de linea
                
                processOption(opcion); // Procesa la opcion
                
            } catch (InputMismatchException e) {
                System.err.println("Error: Debe ingresar un numero.");
                scanner.nextLine(); // Limpiar buffer del scanner
                opcion = -1; // Resetea la opcion para que el loop no termine
            
            } catch (Exception e) {
                // Captura MUY general por si algo mas falla (poco probable)
                System.err.println("Error fatal en el menu: " + e.getMessage());
                opcion = -1; // Resetea
            }

            // PAUSA GLOBAL:
            // Si la opcion no fue "Salir" (0), pausamos la pantalla.
            if (opcion != 0) {
                menuHandler.pausarParaContinuar();
            }
        }
        
        System.out.println("Saliendo de la aplicacion...");
        scanner.close();
    }

    /**
     * Switch principal que delega la accion al MenuHandler.
     * @param opcion La opcion seleccionada por el usuario.
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
                    // La logica de salida ahora esta en el loop run()
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