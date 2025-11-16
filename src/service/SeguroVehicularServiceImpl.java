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

    // ============================================================
    // VALIDACIONES DE NEGOCIO (OBLIGATORIAS)
    // ============================================================
    public void validar(SeguroVehicular seguro) {

        if (seguro == null) {
            throw new IllegalArgumentException("El seguro no puede ser nulo.");
        }

        if (seguro.getAseguradora() == null || seguro.getAseguradora().trim().isEmpty()) {
            throw new IllegalArgumentException("La aseguradora es obligatoria.");
        }

        if (seguro.getNroPoliza() == null || seguro.getNroPoliza().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de póliza es obligatorio.");
        }

        LocalDate hoy = LocalDate.now();
        if (seguro.getVencimiento() == null || !seguro.getVencimiento().isAfter(hoy)) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser FUTURA.");
        }
    }

    // ============================================================
    // VALIDACIÓN DE UNICIDAD
    // ============================================================
    public void validarUnicidadPoliza(String nroPoliza, Connection conn) throws Exception {
        if (seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), conn) != null) {
            throw new IllegalArgumentException("Ya existe un seguro activo con la póliza: " + nroPoliza);
        }
    }

    // ============================================================
    // BUSQUEDA SIMPLE
    // ============================================================
    public SeguroVehicular buscarPorPoliza(String nroPoliza) throws Exception {
        return seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), null);
    }

    // ============================================================
    // INSERTAR (CON FK EXPLÍCITA)
    // ============================================================
    public void insertar(SeguroVehicular seguro, long idVehiculo) throws Exception {

        validar(seguro);

        try (Connection conn = DatabaseConnection.getConnection()) {

            validarUnicidadPoliza(seguro.getNroPoliza(), conn);

            if (idVehiculo <= 0) {
                throw new IllegalArgumentException("ID de vehículo inválido para la creación del seguro.");
            }

            seguroDAO.insertarTx(seguro, idVehiculo, conn);
        }
    }

    // No se usa para B independiente → obligatorio lanzar excepción
    @Override
    public void insertar(SeguroVehicular seguro) throws Exception {
        throw new UnsupportedOperationException("Use insertar(seguro, idVehiculo).");
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @Override
    public void actualizar(SeguroVehicular seguro) throws Exception {

        validar(seguro);

        SeguroVehicular actual = seguroDAO.getById((int) seguro.getId());
        if (actual == null) {
            throw new IllegalArgumentException("El seguro no existe.");
        }

        // Si cambia póliza → verificar unicidad
        if (!actual.getNroPoliza().equalsIgnoreCase(seguro.getNroPoliza())) {
            validarUnicidadPoliza(seguro.getNroPoliza(), null);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            seguroDAO.actualizarTx(seguro, conn);
        }
    }

    // ============================================================
    // ELIMINAR (BAJA LÓGICA)
    // ============================================================
    @Override
    public void eliminar(int id) throws Exception {

        if (seguroDAO.getById(id) == null) {
            throw new IllegalArgumentException("El seguro con ID " + id + " no existe.");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            seguroDAO.eliminarTx(id, conn);
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================
    @Override
    public SeguroVehicular getById(int id) throws Exception {
        return seguroDAO.getById(id);
    }

    @Override
    public List<SeguroVehicular> getAll() throws Exception {
        return seguroDAO.getAll();
    }
}
