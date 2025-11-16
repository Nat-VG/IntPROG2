package service;

import config.DatabaseConnection;
import dao.SeguroVehicularDAO;
import entities.SeguroVehicular;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class SeguroVehicularServiceImpl implements GenericService<SeguroVehicular> {

    private final SeguroVehicularDAO seguroDAO;

    public SeguroVehicularServiceImpl(SeguroVehicularDAO seguroDAO) {
        this.seguroDAO = seguroDAO;
    }

    public void validar(SeguroVehicular seguro) throws IllegalArgumentException {
        if (seguro == null) {
            throw new IllegalArgumentException("El objeto SeguroVehicular no puede ser nulo.");
        }
        if (seguro.getNroPoliza() == null || seguro.getNroPoliza().trim().isEmpty()) {
            throw new IllegalArgumentException("El numero de poliza es obligatorio.");
        }
        if (seguro.getAseguradora() == null || seguro.getAseguradora().trim().isEmpty()) {
            throw new IllegalArgumentException("La aseguradora es obligatoria.");
        }
        if (seguro.getVencimiento() == null || seguro.getVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de vencimiento es invalida (no puede ser anterior a hoy).");
        }
    }

    public void validarUnicidadPoliza(String nroPoliza, Connection conn) throws Exception {
        if (seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), conn) != null) {
            throw new IllegalArgumentException("Ya existe un seguro activo con el numero de poliza: " + nroPoliza);
        }
    }

    public SeguroVehicular buscarPorPoliza(String nroPoliza) throws Exception {
        return seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), null);
    }
    
    // --- MÉTODOS ADAPTADOS PARA EL SERVICE (Versión CRUD simple) ---
    
    public void insertar(SeguroVehicular seguro, long idVehiculo) throws Exception {
        validar(seguro);
        
        try(Connection conn = DatabaseConnection.getConnection()) {
            validarUnicidadPoliza(seguro.getNroPoliza(), conn);
            
            if (idVehiculo <= 0) {
                 throw new IllegalArgumentException("No se puede crear un seguro independiente sin un ID de Vehiculo.");
            }
            
            // Usamos el DAO transaccional, pero manejamos la conexión aquí (no es una transacción compleja)
            seguroDAO.insertarTx(seguro, idVehiculo, conn);
        }
    }
    
    @Override
    public void insertar(SeguroVehicular seguro) throws Exception {
        throw new UnsupportedOperationException("Use el metodo insertar(SeguroVehicular, long idVehiculo) para manejar la FK.");
    }
    
    @Override
    public void actualizar(SeguroVehicular seguro) throws Exception {
        validar(seguro);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Reutilizamos el método transaccional del DAO, abriendo y cerrando la conexión aquí.
            seguroDAO.actualizarTx(seguro, conn);
        }
    }
    
    @Override
    public void eliminar(int id) throws Exception {
        if (seguroDAO.getById(id) == null) {
            throw new Exception("Seguro con ID " + id + " no encontrado o ya eliminado.");
        }
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Reutilizamos el método transaccional del DAO, abriendo y cerrando la conexión aquí.
            seguroDAO.eliminarTx(id, conn);
        }
    }

    @Override
    public SeguroVehicular getById(int id) throws Exception {
        return seguroDAO.getById(id);
    }

    @Override
    public List<SeguroVehicular> getAll() throws Exception {
        return seguroDAO.getAll();
    }
}