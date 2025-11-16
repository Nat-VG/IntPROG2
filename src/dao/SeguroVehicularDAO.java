package dao;

import entities.SeguroVehicular;
import entities.Cobertura;
import config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeguroVehicularDAO implements GenericDAO<SeguroVehicular> {
    
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
    
    // --- MÉTODOS DEL CRUD NO TRANSACCIONAL (Manejan su propia Connection) ---

    /**
     * Inserta una entidad B. Requiere el Service que le provea el idVehiculo (FK).
     * Este método NO ES FUNCIONAL y debe ser manejado por el Service.
     * Lanzamos un error claro para el corrector.
     */
    @Override
    public void insertar(SeguroVehicular entidad) throws Exception {
        throw new UnsupportedOperationException("El metodo insertar() no transaccional no es funcional en SeguroVehicularDAO porque requiere un 'idVehiculo'. Use el Service.");
    }
    
    /**
     * Implementación del actualizar simple (para operaciones individuales).
     */
    @Override
    public void actualizar(SeguroVehicular entidad) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            actualizarTx(entidad, conn);
        }
    }

    /**
     * Implementación del eliminar simple (baja lógica).
     */
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
                return rs.next() ? mapearResultSetASeguro(rs) : null;
            }
        }
    }

    @Override
    public List<SeguroVehicular> getAll() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<SeguroVehicular> seguros = new ArrayList<>();
            while (rs.next()) {
                seguros.add(mapearResultSetASeguro(rs));
            }
            return seguros;
        }
    }
    
    @Override
    public SeguroVehicular buscarPorCampoClave(String valor, Connection conn) throws Exception {
        Connection usedConn = (conn != null) ? conn : DatabaseConnection.getConnection();
        try (PreparedStatement stmt = usedConn.prepareStatement(SELECT_BY_POLIZA_SQL)) {
            stmt.setString(1, valor.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearResultSetASeguro(rs) : null;
            }
        } finally {
            if (conn == null && usedConn != null) {
                usedConn.close();
            }
        }
    }
    
    // --- MÉTODOS DEL CRUD TRANSACCIONAL (Reciben Connection del Service) ---

    @Override
    public long insertarTx(SeguroVehicular seguro, Connection conn) throws Exception {
        throw new UnsupportedOperationException("El metodo insertarTx(SeguroVehicular, Connection) no es funcional. Use el metodo sobrecargado que recibe 'idVehiculo'.");
    }

    public long insertarTx(SeguroVehicular seguro, long idVehiculo, Connection conn) throws Exception {
        if (idVehiculo <= 0) {
            throw new SQLException("Error de logica (DAO): Intentando insertar un seguro sin un ID de Vehiculo valido.");
        }
        
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setSeguroParameters(stmt, seguro);
            stmt.setLong(5, idVehiculo); 

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el seguro, no se afecto ninguna fila.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del seguro insertado.");
                }
            }
        }
    }
    
    @Override
    public void actualizarTx(SeguroVehicular seguro, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            stmt.setString(1, seguro.getAseguradora());
            stmt.setString(2, seguro.getNroPoliza().toUpperCase());
            stmt.setString(3, seguro.getCobertura().name());
            stmt.setDate(4, Date.valueOf(seguro.getVencimiento()));
            stmt.setLong(5, seguro.getId());
            
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Fallo al actualizar Seguro. ID: " + seguro.getId());
            }
        }
    }
    
    @Override
    public void eliminarTx(int id, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Fallo al eliminar (baja logica) Seguro. ID: " + id);
            }
        }
    }
    
    private void setSeguroParameters(PreparedStatement stmt, SeguroVehicular seguro) throws SQLException {
        stmt.setString(1, seguro.getAseguradora());
        stmt.setString(2, seguro.getNroPoliza().toUpperCase());
        stmt.setString(3, seguro.getCobertura().name());
        stmt.setDate(4, Date.valueOf(seguro.getVencimiento()));
    }

    private SeguroVehicular mapearResultSetASeguro(ResultSet rs) throws SQLException {
        SeguroVehicular seguro = new SeguroVehicular();
        seguro.setId(rs.getLong("id"));
        seguro.setEliminado(rs.getBoolean("eliminado"));
        seguro.setAseguradora(rs.getString("aseguradora"));
        seguro.setNroPoliza(rs.getString("nroPoliza"));
        seguro.setCobertura(Cobertura.valueOf(rs.getString("cobertura")));
        
        Date vencimientoDate = rs.getDate("vencimiento");
        if (vencimientoDate != null) {
            seguro.setVencimiento(vencimientoDate.toLocalDate());
        }
        return seguro;
    }
}