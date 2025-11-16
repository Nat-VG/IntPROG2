package dao;

import entities.Vehiculo;
import entities.SeguroVehicular;
import entities.Cobertura;
import config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class VehiculoDAO implements GenericDAO<Vehiculo> {

    public final SeguroVehicularDAO seguroDAO;
    
    public VehiculoDAO(SeguroVehicularDAO seguroDAO) {
        this.seguroDAO = seguroDAO;
    }

    private static final String INSERT_SQL = 
        "INSERT INTO vehiculo (dominio, marca, modelo, anio, nroChasis) VALUES (?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE vehiculo SET dominio = ?, marca = ?, modelo = ?, anio = ?, nroChasis = ? WHERE id = ?";
    
    private static final String DELETE_SQL = 
        "UPDATE vehiculo SET eliminado = TRUE WHERE id = ?";
    
    private static final String SELECT_JOIN_FIELDS = 
        "v.id, v.dominio, v.marca, v.modelo, v.anio, v.nroChasis, v.eliminado, " +
        "s.id AS seguro_id, s.aseguradora, s.nroPoliza, s.cobertura, s.vencimiento, s.eliminado AS seguro_eliminado "; 
        
    private static final String SELECT_ALL_SQL = 
        "SELECT " + SELECT_JOIN_FIELDS + 
        "FROM vehiculo v LEFT JOIN segurovehicular s ON v.id = s.idVehiculo AND s.eliminado = FALSE " + 
        "WHERE v.eliminado = FALSE";

    private static final String SELECT_BY_ID_SQL = 
        "SELECT " + SELECT_JOIN_FIELDS +
        "FROM vehiculo v LEFT JOIN segurovehicular s ON v.id = s.idVehiculo AND s.eliminado = FALSE " + 
        "WHERE v.id = ? AND v.eliminado = FALSE";
        
    private static final String SELECT_BY_DOMINIO_SQL =
        "SELECT " + SELECT_JOIN_FIELDS +
        "FROM vehiculo v LEFT JOIN segurovehicular s ON v.id = s.idVehiculo AND s.eliminado = FALSE " +
        "WHERE v.dominio = ? AND v.eliminado = FALSE";
    
    // --- MÉTODOS DEL CRUD NO TRANSACCIONAL (Manejan su propia Connection) ---
    
    // Dejamos con excepción clara: La inserción de A (Vehiculo) es siempre una operación
    // compuesta que requiere el service.
    @Override
    public void insertar(Vehiculo vehiculo) throws UnsupportedOperationException { 
        throw new UnsupportedOperationException("La insercion de Vehiculo es una operacion compuesta (A + B). Usar el Service transaccional.");
    }
    
    // Dejamos con excepción clara: La actualizacion de A (Vehiculo) debe actualizar B (Seguro).
    @Override
    public void actualizar(Vehiculo vehiculo) throws UnsupportedOperationException { 
        throw new UnsupportedOperationException("La actualizacion de Vehiculo es una operacion compuesta (A + B). Usar el Service transaccional.");
    }
    
    /**
     * Implementación del eliminar simple (para operaciones individuales).
     * NOTA: Este metodo se sigue llamando desde el Service, pero ahora esta implementado.
     */
    @Override
    public void eliminar(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            eliminarTx(id, conn);
        }
    }

    @Override
    public Vehiculo getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearResultSetAVehiculoConSeguro(rs) : null;
            }
        }
    }

    @Override
    public List<Vehiculo> getAll() throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Vehiculo> vehiculos = new ArrayList<>();
            while (rs.next()) {
                vehiculos.add(mapearResultSetAVehiculoConSeguro(rs));
            }
            return vehiculos;
        }
    }
    
    @Override
    public Vehiculo buscarPorCampoClave(String valor, Connection conn) throws Exception {
        Connection usedConn = (conn != null) ? conn : DatabaseConnection.getConnection();
        try (PreparedStatement stmt = usedConn.prepareStatement(SELECT_BY_DOMINIO_SQL)) {
            stmt.setString(1, valor.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearResultSetAVehiculoConSeguro(rs) : null;
            }
        } finally {
            if (conn == null && usedConn != null) {
                usedConn.close();
            }
        }
    }

    // --- MÉTODOS DEL CRUD TRANSACCIONAL (Reciben Connection del Service) ---

    @Override
    public long insertarTx(Vehiculo vehiculo, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setVehiculoParameters(stmt, vehiculo);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el vehiculo, no se afecto ninguna fila.");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID del vehiculo insertado.");
                }
            }
        }
    }

    @Override
    public void actualizarTx(Vehiculo vehiculo, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            setVehiculoParameters(stmt, vehiculo);
            stmt.setLong(6, vehiculo.getId());
            
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Fallo al actualizar Vehiculo. ID: " + vehiculo.getId());
            }
        }
    }

    @Override
    public void eliminarTx(int id, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("Fallo al eliminar (baja logica) Vehiculo. ID: " + id);
            }
        }
    }
    
    private void setVehiculoParameters(PreparedStatement stmt, Vehiculo vehiculo) throws SQLException {
        stmt.setString(1, vehiculo.getDominio().toUpperCase());
        stmt.setString(2, vehiculo.getMarca());
        stmt.setString(3, vehiculo.getModelo());
        stmt.setInt(4, vehiculo.getAnio());
        stmt.setString(5, vehiculo.getNroChasis().toUpperCase());
    }
    
    private Vehiculo mapearResultSetAVehiculoConSeguro(ResultSet rs) throws SQLException {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(rs.getLong("id"));
        vehiculo.setEliminado(rs.getBoolean("eliminado"));
        vehiculo.setDominio(rs.getString("dominio"));
        vehiculo.setMarca(rs.getString("marca"));
        vehiculo.setModelo(rs.getString("modelo"));
        vehiculo.setAnio(rs.getInt("anio"));
        vehiculo.setNroChasis(rs.getString("nroChasis"));
        
        long seguroId = rs.getLong("seguro_id");
        if (!rs.wasNull() && !rs.getBoolean("seguro_eliminado")) {
            SeguroVehicular seguro = new SeguroVehicular();
            seguro.setId(seguroId);
            seguro.setEliminado(rs.getBoolean("seguro_eliminado"));
            seguro.setAseguradora(rs.getString("aseguradora"));
            seguro.setNroPoliza(rs.getString("nroPoliza"));
            seguro.setCobertura(Cobertura.valueOf(rs.getString("cobertura")));
            
            Date vencimientoDate = rs.getDate("vencimiento");
            if (vencimientoDate != null) {
                seguro.setVencimiento(vencimientoDate.toLocalDate());
            }
            
            vehiculo.setSeguro(seguro);
        }
        return vehiculo;
    }
}