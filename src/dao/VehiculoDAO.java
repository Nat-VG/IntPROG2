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
        "s.id AS seguro_id, s.aseguradora, s.nroPoliza, s.cobertura, s.vencimiento, s.eliminado AS seguro_eliminado, s.idVehiculo "; 
        
    private static final String FROM_JOIN_TABLES = 
        "FROM vehiculo v LEFT JOIN segurovehicular s ON v.id = s.idVehiculo ";

    private static final String SELECT_BY_ID_JOIN_SQL = 
        "SELECT " + SELECT_JOIN_FIELDS + FROM_JOIN_TABLES + 
        "WHERE v.id = ? AND v.eliminado = FALSE"; 
        
    private static final String SELECT_ALL_JOIN_SQL = 
        "SELECT " + SELECT_JOIN_FIELDS + FROM_JOIN_TABLES + 
        "WHERE v.eliminado = FALSE";

    private static final String SELECT_BY_DOMINIO_JOIN_SQL = 
        "SELECT " + SELECT_JOIN_FIELDS + FROM_JOIN_TABLES + 
        "WHERE v.dominio = ? AND v.eliminado = FALSE";


    @Override public void insertar(Vehiculo vehiculo) throws UnsupportedOperationException { 
        throw new UnsupportedOperationException("Usar el metodo insertar del VehiculoServiceImpl (transaccional)");
    }
    
    @Override public void actualizar(Vehiculo vehiculo) throws UnsupportedOperationException { 
        throw new UnsupportedOperationException("Usar el metodo actualizar del VehiculoServiceImpl (transaccional)");
    }
    
    @Override public void eliminar(int id) throws UnsupportedOperationException { 
        throw new UnsupportedOperationException("Usar el metodo eliminar del VehiculoServiceImpl (transaccional)");
    }

    @Override
    public Vehiculo getById(int id) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_JOIN_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetAVehiculoConSeguro(rs);
                }
            }
        }
        return null;
    }
    
    @Override
    public List<Vehiculo> getAll() throws Exception {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_JOIN_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                vehiculos.add(mapearResultSetAVehiculoConSeguro(rs));
            }
        }
        return vehiculos;
    }

    @Override
    public Vehiculo buscarPorCampoClave(String dominio, Connection conn) throws Exception {
        boolean closeConn = false;
        Connection actualConn = conn;
        
        if (actualConn == null) {
            actualConn = DatabaseConnection.getConnection();
            closeConn = true;
        }
        
        try (PreparedStatement stmt = actualConn.prepareStatement(SELECT_BY_DOMINIO_JOIN_SQL)) { 
            
            stmt.setString(1, dominio.toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSetAVehiculoConSeguro(rs); 
                }
            }
        } finally {
            if (closeConn && actualConn != null) {
                actualConn.close();
            }
        }
        return null;
    }
    
    @Override
    public long insertarTx(Vehiculo vehiculo, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setVehiculoParameters(stmt, vehiculo);
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long generatedId = generatedKeys.getLong(1);
                    vehiculo.setId(generatedId);
                    return generatedId;
                } else {
                    throw new SQLException("La insercion de Vehiculo fallo, no se obtuvo ID generado.");
                }
            }
        }
    }
    
    @Override
    public void actualizarTx(Vehiculo vehiculo, Connection conn) throws Exception {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            setVehiculoParameters(stmt, vehiculo);
            stmt.setLong(6, vehiculo.getId()); // WHERE id = ?
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
            seguro.setIdVehiculo(rs.getLong("idVehiculo"));
            
            Date vencimientoDate = rs.getDate("vencimiento");
            if(vencimientoDate != null) {
                seguro.setVencimiento(vencimientoDate.toLocalDate());
            }
            
            vehiculo.setSeguro(seguro); 
        }
        
        return vehiculo;
    }
}