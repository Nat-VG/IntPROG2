package dao;

import entities.SeguroVehicular;
import entities.Cobertura;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para SeguroVehicular (Clase B).
 * Corregido para manejar la inserción de la FK 'idVehiculo'
 * desde el objeto SeguroVehicular.
 */
public class SeguroVehicularDAO implements GenericDAO<SeguroVehicular> {
    
    // CORREGIDO: El INSERT_SQL incluye 'idVehiculo'
    private static final String INSERT_SQL = 
        "INSERT INTO segurovehicular (aseguradora, nroPoliza, cobertura, vencimiento, idVehiculo) VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE segurovehicular SET aseguradora = ?, nroPoliza = ?, cobertura = ?, vencimiento = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "UPDATE segurovehicular SET eliminado = TRUE WHERE id = ?";
    
    private static final String SELECT_BY_ID_SQL = 
        "SELECT * FROM segurovehicular WHERE id = ? AND eliminado = FALSE";
    
    private static final String SELECT_ALL_SQL = 
        "SELECT * FROM segurovehicular WHERE eliminado = FALSE";
    
    private static final String SELECT_BY_POLIZA_SQL = 
        "SELECT * FROM segurovehicular WHERE nroPoliza = ? AND eliminado = FALSE";

    // =================================================================
    // MÉTODOS TRANSACCIONALES (Usan la Connection conn externa)
    // =================================================================
    
    @Override
    public long insertarTx(SeguroVehicular seguro, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Llama al método auxiliar corregido
            setSeguroParameters(stmt, seguro);
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long generatedId = generatedKeys.getLong(1);
                    seguro.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("La inserción de Seguro falló, no se obtuvo ID generado.");
                }
            }
        }
    }

    @Override
    public void actualizarTx(SeguroVehicular seguro, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            // Llama al seteo de parámetros (sin FK)
            stmt.setString(1, seguro.getAseguradora());
            stmt.setString(2, seguro.getNroPoliza().toUpperCase());
            stmt.setString(3, seguro.getCobertura().name());
            stmt.setDate(4, Date.valueOf(seguro.getVencimiento()));
            stmt.setLong(5, seguro.getId()); // WHERE id = ?
            stmt.executeUpdate();
        }
    }
    
    @Override
    public void eliminarTx(int id, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    @Override
    public SeguroVehicular buscarPorCampoClave(String nroPoliza, Connection conn) throws Exception {
        boolean closeConn = false;
        Connection actualConn = conn;
        
        if (actualConn == null) {
            actualConn = DatabaseConnection.getConnection();
            closeConn = true;
        }
        
        try (PreparedStatement stmt = actualConn.prepareStatement(SELECT_BY_POLIZA_SQL)) {
            stmt.setString(1, nroPoliza.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetASeguro(rs);
                }
            }
        } finally {
            if (closeConn && actualConn != null) {
                actualConn.close();
            }
        }
        return null;
    }

    // =================================================================
    // MÉTODOS SIN TRANSACCIÓN (Manejan su propia Connection)
    // =================================================================

    @Override
    public void insertar(SeguroVehicular entidad) throws Exception {
        // Esta versión (no transaccional) requiere que el idVehiculo ya esté seteado
        try (Connection conn = DatabaseConnection.getConnection()) {
            insertarTx(entidad, conn);
        }
    }

    @Override
    public void actualizar(SeguroVehicular entidad) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            actualizarTx(entidad, conn);
        }
    }

    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            eliminarTx(id, conn);
        }
    }

    @Override
    public SeguroVehicular getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetASeguro(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<SeguroVehicular> getAll() throws Exception {
        List<SeguroVehicular> seguros = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                seguros.add(mapearResultSetASeguro(rs));
            }
        }
        return seguros;
    }
    
    // --- MÉTODOS AUXILIARES ---
    
    /**
     * Setea los parámetros del Seguro en un PreparedStatement (para INSERT).
     * CORREGIDO: Usa seguro.getIdVehiculo() para setear la FK.
     * @param stmt El PreparedStatement a configurar.
     * @param seguro El objeto con los datos (debe tener idVehiculo seteado).
     * @throws SQLException Si falla el seteo o idVehiculo no es válido.
     */
    private void setSeguroParameters(PreparedStatement stmt, SeguroVehicular seguro) throws SQLException {
        stmt.setString(1, seguro.getAseguradora());
        stmt.setString(2, seguro.getNroPoliza().toUpperCase());
        stmt.setString(3, seguro.getCobertura().name()); // Convertimos enum a string
        stmt.setDate(4, Date.valueOf(seguro.getVencimiento()));
        
        // CORRECCIÓN CRÍTICA:
        if (seguro.getIdVehiculo() <= 0) {
            throw new SQLException("Error de lógica (DAO): Intentando insertar un seguro sin un ID de Vehículo válido.");
        }
        stmt.setLong(5, seguro.getIdVehiculo()); // <-- Se usa la FK
    }

    /**
     * Mapea una fila de ResultSet a un objeto SeguroVehicular.
     * @param rs El ResultSet posicionado en la fila a mapear.
     * @return El objeto SeguroVehicular.
     * @throws SQLException Si falla la lectura del ResultSet.
     */
    private SeguroVehicular mapearResultSetASeguro(ResultSet rs) throws SQLException {
        SeguroVehicular seguro = new SeguroVehicular();
        seguro.setId(rs.getLong("id"));
        seguro.setEliminado(rs.getBoolean("eliminado"));
        seguro.setAseguradora(rs.getString("aseguradora"));
        seguro.setNroPoliza(rs.getString("nroPoliza"));
        seguro.setCobertura(Cobertura.valueOf(rs.getString("cobertura")));
        seguro.setIdVehiculo(rs.getLong("idVehiculo")); // Carga la FK
        
        Date vencimientoDate = rs.getDate("vencimiento");
        if (vencimientoDate != null) {
            seguro.setVencimiento(vencimientoDate.toLocalDate());
        }
        return seguro;
    }
}