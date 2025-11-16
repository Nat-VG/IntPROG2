package main;

/**
 * Clase utilitaria (solo métodos estáticos) para mostrar el menú de la aplicación.
 */
public class MenuDisplay {

    /**
     * Muestra el menú principal con las opciones del TPI de Vehículos.
     */
    public static void mostrarMenuPrincipal() {
        System.out.println("\n+---------------------------------------------------+");
        System.out.println("|      *** GESTION DE FLOTA VEHICULAR (TPI) *** |");
        System.out.println("+---------------------------------------------------+");
        System.out.println("|                                                   |");
        System.out.println("|    VEHICULOS (CRUD Compuesto A + B)               |");
        System.out.println("|---------------------------------------------------|");
        System.out.println("| 1. Crear Vehiculo (con Seguro, Transaccional)     |");
        System.out.println("| 2. Listar todos los Vehiculos                     |");
        System.out.println("| 3. Buscar Vehiculo por ID (con Seguro)            |");
        System.out.println("| 4. Actualizar Vehiculo (y su Seguro)              |");
        System.out.println("| 5. Eliminar Vehiculo (Baja Logica A y B)          |");
        System.out.println("|                                                   |");
        System.out.println("|    SEGUROS (CRUD Individual B)                    |");
        System.out.println("|---------------------------------------------------|");
        System.out.println("| 6. Crear Seguro (para Vehiculo existente)         |"); 
        System.out.println("| 7. Actualizar Seguro por ID                       |"); 
        System.out.println("| 8. Eliminar Seguro por ID (Baja Logica)           |"); 
        System.out.println("| 9. Listar todos los Seguros                       |"); 
        System.out.println("|                                                   |");
        System.out.println("|    BUSQUEDAS POR CAMPO CLAVE                      |");
        System.out.println("|---------------------------------------------------|");
        System.out.println("| 10. Buscar Vehiculo por Dominio (Patente)         |"); 
        System.out.println("| 11. Buscar Seguro por Nro. de Poliza              |"); 
        System.out.println("|                                                   |");
        System.out.println("+---------------------------------------------------+");
        System.out.println("| 0. Salir                                          |");
        System.out.println("+---------------------------------------------------+");
        
        System.out.print("\n  Ingrese una opcion: ");
    }
}