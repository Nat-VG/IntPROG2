package config; 

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestiona el ciclo de vida de una transacción (Tx) sobre una Connection JDBC.
 * Encapsula la lógica de setAutoCommit(false), commit, y rollback.
 *
 * CRÍTICO: Implementa AutoCloseable para ser usado con try-with-resources.
 * Esto garantiza que el rollback() se ejecute automáticamente si el bloque try
 * falla antes de un commit() explícito.
 */
public class TransactionManager implements AutoCloseable {

    private Connection conn;
    private boolean transactionActive;

    /**
     * Constructor que recibe la conexión que gestionará.
     * @param conn La conexión JDBC obtenida desde DatabaseConnection.
     * @throws IllegalArgumentException Si la conexión es nula.
     */
    public TransactionManager(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("La conexión no puede ser null.");
        }
        this.conn = conn;
        this.transactionActive = false; // La Tx no empieza hasta que se llama a startTransaction()
    }

    /**
     * Devuelve la conexión gestionada para que los DAOs puedan usarla.
     * @return La Connection activa.
     */
    public Connection getConnection() {
        return conn;
    }

    /**
     * Inicia la transacción desactivando el modo auto-commit.
     * Este es el "punto de no retorno" (hasta el commit/rollback).
     * @throws SQLException Si falla la operación de BD.
     */
    public void startTransaction() throws SQLException {
        if (conn == null || conn.isClosed()) {
            throw new SQLException("No se puede iniciar la transacción: la conexión no está disponible.");
        }
        conn.setAutoCommit(false);
        transactionActive = true;
    }

    /**
     * Confirma la transacción. Persiste todos los cambios realizados
     * (ej. INSERT en Seguro, INSERT en Vehiculo, UPDATE de FK).
     * @throws SQLException Si falla la operación de commit.
     */
    public void commit() throws SQLException {
        if (conn == null) {
            throw new SQLException("Error al hacer commit: no hay conexión establecida.");
        }
        if (!transactionActive) {
            throw new SQLException("No hay una transacción activa para hacer commit.");
        }
        conn.commit();
        transactionActive = false; // La transacción ha terminado
    }

    /**
     * Deshace la transacción. Revierte todos los cambios pendientes.
     * Es esencial llamarlo dentro del bloque catch en la capa Service,
     * aunque close() lo llama como salvaguarda.
     */
    public void rollback() {
        if (conn != null && transactionActive) {
            try {
                conn.rollback();
                transactionActive = false;
            } catch (SQLException e) {
                // Error grave: El rollback falló. Loguear el error.
                System.err.println("Error MUY GRAVE durante el rollback: " + e.getMessage());
            }
        }
    }

    /**
     * Implementación de AutoCloseable (método close()).
     * Se llama automáticamente al salir del bloque try-with-resources.
     *
     * 1. Si la Tx sigue activa (porque hubo un error y no se hizo commit), hace rollback.
     * 2. Restaura el autoCommit a true (buena práctica).
     * 3. Cierra la conexión.
     */
    @Override
    public void close() {
        if (conn != null) {
            try {
                if (transactionActive) {
                    // Si el programador olvidó el commit/rollback (ej. por una excepción),
                    // aseguramos el rollback para no dejar la BD inconsistente.
                    System.err.println("Advertencia: Transacción cerrada sin commit. Ejecutando rollback automático.");
                    rollback();
                }
                // Siempre restauramos el modo autoCommit antes de devolver la conexión al pool (o cerrarla)
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error al restaurar autoCommit: " + e.getMessage());
            } finally {
                try {
                    conn.close(); // Cierra la conexión física
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }
}