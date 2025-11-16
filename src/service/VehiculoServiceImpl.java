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

    // ============================================================
    // VALIDACIONES DE NEGOCIO (OBLIGATORIAS POR CONSIGNA)
    // ============================================================
    private void validar(Vehiculo vehiculo) {

        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehiculo no puede ser nulo.");
        }

        if (vehiculo.getDominio() == null || vehiculo.getDominio().trim().isEmpty()) {
            throw new IllegalArgumentException("El dominio es obligatorio.");
        }

        if (vehiculo.getMarca() == null || vehiculo.getMarca().trim().isEmpty()) {
            throw new IllegalArgumentException("La marca es obligatoria.");
        }

        if (vehiculo.getModelo() == null || vehiculo.getModelo().trim().isEmpty()) {
            throw new IllegalArgumentException("El modelo es obligatorio.");
        }

        int anio = vehiculo.getAnio();
        int actual = LocalDate.now().getYear();
        if (anio < 1950 || anio > actual + 1) {
            throw new IllegalArgumentException("El año debe estar entre 1950 y " + (actual + 1));
        }

        if (vehiculo.getNroChasis() == null || vehiculo.getNroChasis().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de chasis es obligatorio.");
        }
    }

    // ============================================================
    // VALIDACION DE UNICIDAD DE DOMINIO
    // ============================================================
    public void validarUnicidad(String dominio) throws Exception {
        // 🔥 ESTA ES LA FIRMA CORRECTA SEGÚN TU DAO
        if (vehiculoDAO.buscarPorCampoClave(dominio.toUpperCase(), null) != null) {
            throw new IllegalArgumentException("Ya existe un vehiculo activo con el dominio: " + dominio);
        }
    }

    // ============================================================
    // INSERTAR (Vehículo + Seguro en una única transacción)
    // ============================================================
    @Override
    public void insertar(Vehiculo vehiculo) throws Exception {

        validar(vehiculo);
        validarUnicidad(vehiculo.getDominio());

        // Validamos el seguro si viene en el objeto
        if (vehiculo.getSeguro() != null) {
            seguroService.validar(vehiculo.getSeguro());
            seguroService.validarUnicidadPoliza(vehiculo.getSeguro().getNroPoliza(), null);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();

            // 1 — Insertamos el vehículo
            long vehiculoId = vehiculoDAO.insertarTx(vehiculo, tm.getConnection());
            vehiculo.setId(vehiculoId);

            // 2 — Insertamos el seguro si existe
            if (vehiculo.getSeguro() != null) {
                SeguroVehicular seguro = vehiculo.getSeguro();

                long seguroId = vehiculoDAO.seguroDAO.insertarTx(
                        seguro, vehiculoId, tm.getConnection()
                );
                seguro.setId(seguroId);
            }

            tm.commit();

        } catch (Exception e) {
            throw new Exception("Error en la transaccion de insercion: " + e.getMessage());
        }
    }

    // ============================================================
    // ACTUALIZAR (Vehículo + Seguro)
    // ============================================================
    @Override
    public void actualizar(Vehiculo vehiculo) throws Exception {

        validar(vehiculo);

        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();

            // Actualizar A
            vehiculoDAO.actualizarTx(vehiculo, tm.getConnection());

            // Actualizar B si hay seguro
            if (vehiculo.getSeguro() != null) {
                seguroService.validar(vehiculo.getSeguro());
                vehiculoDAO.seguroDAO.actualizarTx(vehiculo.getSeguro(), tm.getConnection());
            }

            tm.commit();

        } catch (Exception e) {
            throw new Exception("Error en la transaccion de actualizacion: " + e.getMessage());
        }
    }

    // ============================================================
    // ELIMINAR (Baja lógica A + B)
    // ============================================================
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
            throw new Exception("Error en la transaccion de eliminacion: " + e.getMessage());
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================
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
