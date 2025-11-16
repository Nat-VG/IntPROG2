package main;

/**
 * Clase utilitaria (solo métodos estáticos) para mostrar el menú de la aplicación.
 *
 * CORREGIDO:
 * 1. Se eliminaron los acentos (ej. "Vehículos") para evitar
 * errores de codificación (ej. "VEHICULOS") en la consola.
 * 2. Se añadió un formato de cuadro ASCII para mejorar la estética.
 *
 * @author [Tu Nombre/Grupo Aquí]
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
        System.out.println("| VEHICULOS (CRUD Transaccional 1:1)                |");
        System.out.println("|---------------------------------------------------|");
        System.out.println("| 1. Crear Vehiculo (con Seguro)                    |");
        System.out.println("| 2. Listar todos los Vehiculos (con Seguros)       |");
        System.out.println("| 3. Buscar Vehiculo por ID (con Seguro)            |");
        System.out.println("| 4. Actualizar Vehiculo (y su Seguro)              |");
        System.out.println("| 5. Eliminar Vehiculo (Baja Logica A y B)          |");
        System.out.println("|                                                   |");
        System.out.println("| BUSQUEDAS Y GESTION INDIVIDUAL                    |");
        System.out.println("|---------------------------------------------------|");
        System.out.println("| 6. Buscar Vehiculo por Dominio (Patente)          |");
        System.out.println("| 7. Buscar Seguro por Nro. de Poliza               |");
        System.out.println("| 8. Crear Seguro (para un vehiculo existente)      |");
        System.out.println("| 9. Listar todos los Seguros                       |");
        System.out.println("|                                                   |");
        System.out.println("+---------------------------------------------------+");
        System.out.println("| 0. Salir                                          |");
        System.out.println("+---------------------------------------------------+");
        
        // El "\n" inicial y el espacio extra hacen que sea más legible
        System.out.print("\n  Ingrese una opcion: ");
    }
}