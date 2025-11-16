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
    
    public void validarUnicidad(String dominio) throws Exception {
        if (vehiculoDAO.buscarPorCampoClave(dominio.toUpperCase(), null) != null) {
            throw new IllegalArgumentException("Ya existe un vehiculo activo con el dominio: " + dominio);
        }
    }

    /**
     * Inserta un Vehiculo y, opcionalmente, su Seguro asociado en una única transacción.
     */
    @Override
    public void insertar(Vehiculo vehiculo) throws Exception {
        validar(vehiculo);
        validarUnicidad(vehiculo.getDominio());
        
        // 1. Validar unicidad de la Poliza (si hay seguro) y otros campos de B
        if (vehiculo.getSeguro() != null) {
            seguroService.validar(vehiculo.getSeguro());
            // Se debe validar la unicidad de la poliza DENTRO de la transaccion
            // para evitar race conditions, pero por simplicidad se hace aqui con una conexion simple
            // (el DAO que busca por clave ya usa su propia conexion).
            seguroService.validarUnicidadPoliza(vehiculo.getSeguro().getNroPoliza(), null);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();

            // 2. Insertar Vehiculo (Clase A)
            long vehiculoId = vehiculoDAO.insertarTx(vehiculo, tm.getConnection());
            vehiculo.setId(vehiculoId); // Asegurar que el objeto tiene el ID generado

            // 3. Insertar Seguro (Clase B) si existe
            if (vehiculo.getSeguro() != null) {
                SeguroVehicular seguro = vehiculo.getSeguro();
                
                // --- LLAMADA CORREGIDA: Pasar el ID del Vehiculo al DAO para la FK ---
                long seguroId = vehiculoDAO.seguroDAO.insertarTx(seguro, vehiculoId, tm.getConnection());
                seguro.setId(seguroId);
            }

            tm.commit();

        } catch (Exception e) {
            System.err.println("La transaccion de insercion fallo. Rollback asegurado.");
            throw new Exception("Error en la transaccion: " + e.getMessage());
        }
    }
    
    @Override
    public void actualizar(Vehiculo vehiculo) throws Exception {
        validar(vehiculo);
        
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();

            // 1. Actualizar Vehiculo (A)
            vehiculoDAO.actualizarTx(vehiculo, tm.getConnection());

            // 2. Actualizar Seguro (B) si existe
            if (vehiculo.getSeguro() != null) {
                seguroService.validar(vehiculo.getSeguro());
                vehiculoDAO.seguroDAO.actualizarTx(vehiculo.getSeguro(), tm.getConnection());
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
            
            // 1. Baja logica de Vehiculo (A)
            vehiculoDAO.eliminarTx(id, tm.getConnection());
            
            // 2. Baja logica de Seguro (B) si existe
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