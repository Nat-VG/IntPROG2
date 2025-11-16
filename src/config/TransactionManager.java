package config;

import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager implements AutoCloseable {

    private Connection conn;
    private boolean transactionActive;

    public TransactionManager(Connection conn) throws SQLException {
        if (conn == null) {
            throw new IllegalArgumentException("La conexion no puede ser null.");
        }
        this.conn = conn;
        this.transactionActive = false;
    }

    public Connection getConnection() {
        return conn;
    }

    public void startTransaction() throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("No se puede iniciar la transaccion: la conexion no esta disponible.");
        }
        conn.setAutoCommit(false);
        transactionActive = true;
    }

    public void commit() throws SQLException {
        if (conn == null || !transactionActive) {
            throw new SQLException("No hay una transaccion activa para hacer commit.");
        }
        conn.commit();
        transactionActive = false;
    }

    public void rollback() {
        if (conn != null && transactionActive) {
            try {
                conn.rollback();
                transactionActive = false;
            } catch (SQLException e) {
                System.err.println("Error MUY GRAVE durante el rollback: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                if (transactionActive) {
                    System.err.println("Advertencia: Transaccion cerrada sin commit. Ejecutando rollback automatico.");
                    rollback();
                }
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autoCommit: " + e.getMessage());
            } finally {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexion: " + e.getMessage());
                }
            }
        }
    }
}