package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Esta clase se encarga de conectarnos a la base de datos MySQL
// Usamos el patrón Singleton: solo una conexión a la vez
public final class DatabaseConnection {
    // Datos de conexión - los mismos que usaste en los scripts SQL
    private static final String URL = "jdbc:mysql://localhost:3306/BaseVehiculos";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // poner la contraseña

    // Bloque estático: se ejecuta cuando la clase se carga por primera vez
    static {
        try {
            // Cargamos el driver de MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el driver de MySQL: " + e.getMessage());
        }
    }

    // Constructor privado: evitamos que se creen instancias de esta clase
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria, no se puede instanciar");
    }

    // Método principal: nos da una conexión a la base de datos
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}