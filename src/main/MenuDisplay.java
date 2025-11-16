package main; //

/**
 * Clase utilitaria (solo métodos estáticos) para mostrar el menú de la aplicación.
 * No tiene estado ni lógica, solo imprime en consola.
 */
public class MenuDisplay {

    /**
     * Muestra el menú principal con las opciones del TPI de Vehículos.
     */
    public static void mostrarMenuPrincipal() {
        System.out.println("\n========= GESTIÓN DE FLOTA VEHICULAR (TPI) =========");
        
        // --- Opciones de Vehículos (Clase A) ---
        System.out.println("--- VEHÍCULOS (CRUD 1:1) ---");
        System.out.println("1. Crear Vehículo (con Seguro) (Op. Transaccional)");
        System.out.println("2. Listar todos los Vehículos (con Seguros)");
        System.out.println("3. Buscar Vehículo por ID (con Seguro)");
        System.out.println("4. Actualizar Vehículo (y su Seguro) (Op. Transaccional)");
        System.out.println("5. Eliminar Vehículo (Baja Lógica de A y B) (Op. Transaccional)");
        
        // --- Búsquedas (Requeridas por TPI) ---
        System.out.println("--- BÚSQUEDAS ---");
        System.out.println("6. Buscar Vehículo por Dominio (Patente)");
        System.out.println("7. Buscar Seguro por Nro. de Póliza");

        // --- Opciones de Seguros (Clase B - Opcional) ---
        System.out.println("--- GESTIÓN DE SEGUROS (INDIVIDUAL) ---");
        System.out.println("8. Crear Seguro (sin asociar)");
        System.out.println("9. Listar todos los Seguros");
        
        System.out.println("-----------------------------------------------------");
        System.out.println("0. Salir");
        System.out.print("Ingrese una opción: ");
    }
}