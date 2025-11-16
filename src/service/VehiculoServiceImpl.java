package service;

import config.DatabaseConnection;
import config.TransactionManager;
import dao.VehiculoDAO;
import entities.Vehiculo;
import entities.SeguroVehicular;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public class VehiculoServiceImpl implements GenericService<Vehiculo> {

    private final VehiculoDAO vehiculoDAO;
    private final SeguroVehicularServiceImpl seguroService;

    public VehiculoServiceImpl(VehiculoDAO vehiculoDAO, SeguroVehicularServiceImpl seguroService) {
        this.vehiculoDAO = vehiculoDAO;
        this.seguroService = seguroService;
    }
    
    private void validar(Vehiculo vehiculo) throws IllegalArgumentException {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El objeto Vehiculo no puede ser nulo.");
        }
        if (vehiculo.getDominio() == null || vehiculo.getDominio().trim().isEmpty()) {
            throw new IllegalArgumentException("El dominio (patente) es obligatorio.");
        }
        if (vehiculo.getMarca() == null || vehiculo.getMarca().trim().isEmpty()) {
            throw new IllegalArgumentException("La marca es obligatoria.");
        }
        if (vehiculo.getAnio() > LocalDate.now().getYear() + 1 || vehiculo.getAnio() < 1950) {
            throw new IllegalArgumentException("El ano de fabricacion es invalido.");
        }
        if (vehiculo.getNroChasis() == null || vehiculo.getNroChasis().trim().isEmpty()) {
            throw new IllegalArgumentException("El numero de chasis es obligatorio.");
        }
    }

    private void validarUnicidad(Vehiculo vehiculo, Connection conn) throws Exception {
        if (vehiculoDAO.buscarPorCampoClave(vehiculo.getDominio().toUpperCase(), conn) != null) {
            throw new IllegalArgumentException("El dominio '" + vehiculo.getDominio() + "' ya existe en el sistema.");
        }
    }
    
    @Override
    public void insertar(Vehiculo vehiculo) throws Exception {
        validar(vehiculo);
        
        if (vehiculo.getSeguro() == null) {
            throw new IllegalArgumentException("El vehiculo debe tener un seguro asociado para ser creado.");
        }
        
        seguroService.validar(vehiculo.getSeguro());

        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();
            
            validarUnicidad(vehiculo, tm.getConnection());
            seguroService.validarUnicidadPoliza(vehiculo.getSeguro().getNroPoliza(), tm.getConnection());
            
            vehiculoDAO.insertarTx(vehiculo, tm.getConnection());
            
            long idVehiculoGenerado = vehiculo.getId();
            SeguroVehicular seguro = vehiculo.getSeguro();
            seguro.setIdVehiculo(idVehiculoGenerado);
            
            vehiculoDAO.seguroDAO.insertarTx(seguro, tm.getConnection());

            tm.commit();
            
        } catch (Exception e) {
            System.err.println("La transaccion de insercion (Vehiculo/Seguro) fallo. Rollback asegurado.");
            throw new Exception("Error en la transaccion: " + e.getMessage()); 
        }
    }
    
    @Override
    public void actualizar(Vehiculo vehiculo) throws Exception { 
        validar(vehiculo);
        
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();
            
            vehiculoDAO.actualizarTx(vehiculo, tm.getConnection());
            
            if (vehiculo.getSeguro() != null) {
                SeguroVehicular seguro = vehiculo.getSeguro();
                seguroService.validar(seguro);
                
                if (seguro.getId() == 0) {
                     throw new IllegalArgumentException("No se puede crear un nuevo seguro en una actualizacion. La relacion 1:1 es fija.");
                } else {
                    vehiculoDAO.seguroDAO.actualizarTx(seguro, tm.getConnection());
                }
            }

            tm.commit();
        } catch (Exception e) {
            System.err.println("La transaccion de actualizacion fallo. Rollback asegurado.");
            throw new Exception("Error en la transaccion: " + e.getMessage());
        }
    }
    
    @Override
    public void eliminar(int id) throws Exception { 
        Vehiculo vehiculo = vehiculoDAO.getById(id); 
        
        if (vehiculo == null) {
            throw new Exception("Vehiculo con ID " + id + " no encontrado o ya eliminado.");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();
            
            vehiculoDAO.eliminarTx(id, tm.getConnection());
            
            if (vehiculo.getSeguro() != null) {
                int seguroId = (int) vehiculo.getSeguro().getId();
                vehiculoDAO.seguroDAO.eliminarTx(seguroId, tm.getConnection());
            }

            tm.commit();
            
        } catch (Exception e) {
            System.err.println("La transaccion de eliminacion (baja logica) fallo. Rollback asegurado.");
            throw new Exception("Error en la transaccion: " + e.getMessage());
        }
    }
    
    @Override
    public Vehiculo getById(int id) throws Exception { 
        return vehiculoDAO.getById(id);
    }
    
    @Override
    public List<Vehiculo> getAll() throws Exception { 
        return vehiculoDAO.getAll();
    }
    
    public Vehiculo buscarPorDominio(String dominio) throws Exception {
        return vehiculoDAO.buscarPorCampoClave(dominio.toUpperCase(), null);
    }
}