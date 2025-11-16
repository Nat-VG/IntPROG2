package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    
    private static final String URL = "jdbc:mysql://localhost:3306/BaseVehiculos";
    private static final String USER = "root";
    
    // ATENCION: Poner tu contrasena de MySQL aqui
    private static final String PASSWORD = "1234"; // O la contrasena que uses

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontro el driver de MySQL (com.mysql.cj.jdbc.Driver). Asegurate de anadir el JAR a las bibliotecas del proyecto.");
        }
    }

    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta es una clase utilitaria, no se puede instanciar");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}