package service;

import config.DatabaseConnection;
import config.TransactionManager; // <-- IMPORTADO PARA EL PROBLEMA 1
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
    // (Sin cambios)
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
    // (Sin cambios)
    // ============================================================
    public void validarUnicidadPoliza(String nroPoliza, Connection conn) throws Exception {
        if (seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), conn) != null) {
            throw new IllegalArgumentException("Ya existe un seguro activo con la póliza: " + nroPoliza);
        }
    }

    // ============================================================
    // BUSQUEDA SIMPLE
    // (Sin cambios)
    // ============================================================
    public SeguroVehicular buscarPorPoliza(String nroPoliza) throws Exception {
        return seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), null);
    }

    // ============================================================
    // INSERTAR (CON FK EXPLÍCITA)
    // --- CORREGIDO CON TRANSACTIONMANAGER Y SETEO DE ID ---
    // ============================================================
    public void insertar(SeguroVehicular seguro, long idVehiculo) throws Exception {

        validar(seguro);

        if (idVehiculo <= 0) {
            throw new IllegalArgumentException("ID de vehículo inválido para la creación del seguro.");
        }
        
        // --- INICIO CORRECCIÓN (PROBLEMA 1 Y 2) ---
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();

            // La validación de unicidad AHORA usa la conexión transaccional
            validarUnicidadPoliza(seguro.getNroPoliza(), tm.getConnection());

            // 1. Capturamos el ID que retorna el DAO
            long nuevoId = seguroDAO.insertarTx(seguro, idVehiculo, tm.getConnection());
            
            // 2. Seteamos ese ID en el objeto original (Solución ID: 0)
            seguro.setId(nuevoId); 

            tm.commit();

        } catch (Exception e) {
            // El rollback es automático
            throw new Exception("Error en la transaccion de insercion del seguro: " + e.getMessage());
        }
        // --- FIN CORRECCIÓN ---
    }

    // No se usa para B independiente → obligatorio lanzar excepción
    @Override
    public void insertar(SeguroVehicular seguro) throws Exception {
        throw new UnsupportedOperationException("Use insertar(seguro, idVehiculo).");
    }

    // ============================================================
    // ACTUALIZAR
    // --- CORREGIDO CON TRANSACTIONMANAGER ---
    // ============================================================
    @Override
    public void actualizar(SeguroVehicular seguro) throws Exception {

        validar(seguro);

        SeguroVehicular actual = seguroDAO.getById((int) seguro.getId());
        if (actual == null) {
            throw new IllegalArgumentException("El seguro que intenta actualizar no existe.");
        }

        // --- INICIO CORRECCIÓN (PROBLEMA 1) ---
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {
            
            tm.startTransaction();

            // Si cambia póliza → verificar unicidad DENTRO de la transacción
            if (!actual.getNroPoliza().equalsIgnoreCase(seguro.getNroPoliza())) {
                validarUnicidadPoliza(seguro.getNroPoliza(), tm.getConnection());
            }

            // El DAO usa la conexión transaccional
            seguroDAO.actualizarTx(seguro, tm.getConnection());

            tm.commit();

        } catch (Exception e) {
            // El rollback es automático
            throw new Exception("Error en la transaccion de actualizacion del seguro: " + e.getMessage());
        }
        // --- FIN CORRECCIÓN ---
    }

// ============================================================
    // ELIMINAR (BAJA LÓGICA)
    // --- CORREGIDO CON TRANSACTIONMANAGER ---
    // ============================================================
    @Override
    public void eliminar(int id) throws Exception {

        // Validamos la existencia ANTES de iniciar la transacción
        if (seguroDAO.getById(id) == null) {
            throw new IllegalArgumentException("El seguro con ID " + id + " no existe o ya fue eliminado.");
        }

        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();
            
            // El DAO usa la conexión transaccional
            seguroDAO.eliminarTx(id, tm.getConnection());
            
            tm.commit();

        } catch (Exception e) {
            // --- INICIO DE LA CORRECCIÓN ---
            // Faltaba el "+" y "e.getMessage()"
            throw new Exception("Error en la transaccion de eliminacion del seguro: " + e.getMessage());
            // --- FIN DE LA CORRECCIÓN ---
        }
    }

    // ============================================================
    // GETTERS (Lectura, no requieren transacción)
    // (Sin cambios)
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