package service;

import config.DatabaseConnection;
import config.TransactionManager;
import dao.VehiculoDAO;
import entities.Vehiculo;
import entities.SeguroVehicular;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la entidad Vehiculo (Clase A).
 * ORQUESTADOR TRANSACCIONAL: Maneja la lógica de negocio y garantiza la
 * atomicidad (commit/rollback) de las operaciones 1:1 con SeguroVehicular.
 *
 * @author [Tu Nombre/Grupo Aquí]
 */
public class VehiculoServiceImpl implements GenericService<Vehiculo> {

    // Dependencias inyectadas (DI)
    private final VehiculoDAO vehiculoDAO;
    private final SeguroVehicularServiceImpl seguroService;

    /**
     * Constructor para Inyección de Dependencias.
     * @param vehiculoDAO El DAO de Vehiculo (A).
     * @param seguroService El Service de Seguro (B), necesario para validaciones.
     */
    public VehiculoServiceImpl(VehiculoDAO vehiculoDAO, SeguroVehicularServiceImpl seguroService) {
        this.vehiculoDAO = vehiculoDAO;
        this.seguroService = seguroService;
    }
    
    // =================================================================
    // MÉTODOS DE VALIDACIÓN (Lógica de Negocio)
    // =================================================================

    /**
     * Valida los campos obligatorios y el formato de un Vehículo.
     * @param vehiculo El objeto a validar.
     * @throws IllegalArgumentException Si la validación falla.
     */
    private void validar(Vehiculo vehiculo) throws IllegalArgumentException {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El objeto Vehículo no puede ser nulo.");
        }
        if (vehiculo.getDominio() == null || vehiculo.getDominio().trim().isEmpty()) {
            throw new IllegalArgumentException("El dominio (patente) es obligatorio.");
        }
        if (vehiculo.getMarca() == null || vehiculo.getMarca().trim().isEmpty()) {
            throw new IllegalArgumentException("La marca es obligatoria.");
        }
        if (vehiculo.getAnio() > LocalDate.now().getYear() + 1 || vehiculo.getAnio() < 1950) {
            // (Permitimos hasta el año que viene por 0km)
            throw new IllegalArgumentException("El año de fabricación es inválido.");
        }
        if (vehiculo.getNroChasis() == null || vehiculo.getNroChasis().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de chasis es obligatorio.");
        }
    }

    /**
     * Valida la unicidad de campos clave (Dominio) dentro de una Tx.
     * @param vehiculo El objeto a validar.
     * @param conn La conexión transaccional.
     * @throws Exception Si un campo ya existe.
     */
    private void validarUnicidad(Vehiculo vehiculo, Connection conn) throws Exception {
        if (vehiculoDAO.buscarPorCampoClave(vehiculo.getDominio().toUpperCase(), conn) != null) {
            throw new IllegalArgumentException("El dominio '" + vehiculo.getDominio() + "' ya existe en el sistema.");
        }
        // (Añadir validación de unicidad para NroChasis si es necesario)
    }
    
    // =================================================================
    // OPERACIONES CRUD TRANSACCIONALES (A y B)
    // =================================================================

    /**
     * CRÍTICO (TPI): Inserta un Vehiculo (A) y su Seguro (B) de forma ATÓMICA.
     * Flujo transaccional (A -> B, con FK en B):
     * 1. Iniciar Transacción (setAutoCommit(false))
     * 2. Validar A (Campos) y B (Campos)
     * 3. Validar Unicidad A (Dominio) y B (NroPoliza)
     * 4. Insertar A (Vehiculo) -> Obtener ID_A
     * 5. Setear ID_A en el objeto B (seguro.setIdVehiculo(ID_A))
     * 6. Insertar B (Seguro), que ahora tiene la FK
     * 7. Commit
     * Si algo falla -> Rollback (automático por TransactionManager.close())
     *
     * @param vehiculo La entidad a persistir (debe contener el Seguro).
     * @throws Exception Si falla la validación o la persistencia.
     */
    @Override
    public void insertar(Vehiculo vehiculo) throws Exception {
        // 2. Validar A (Campos)
        validar(vehiculo);
        
        if (vehiculo.getSeguro() == null) {
            throw new IllegalArgumentException("El vehículo debe tener un seguro asociado para ser creado.");
        }
        
        // 2. Validar B (Campos)
        seguroService.validar(vehiculo.getSeguro());

        // 1. Iniciar Transacción
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction(); // CRÍTICO: setAutoCommit(false)
            
            // 3. Validar Unicidad de A (Dominio)
            validarUnicidad(vehiculo, tm.getConnection());
            
            // 3. Validar Unicidad de B (NroPoliza)
            seguroService.validarUnicidadPoliza(vehiculo.getSeguro().getNroPoliza(), tm.getConnection());
            
            // 4. Insertar A (Vehiculo) PRIMERO
            // (El DAO setea el ID en el objeto 'vehiculo')
            vehiculoDAO.insertarTx(vehiculo, tm.getConnection());
            
            // 5. Setear ID_A en B
            long idVehiculoGenerado = vehiculo.getId();
            SeguroVehicular seguro = vehiculo.getSeguro();
            seguro.setIdVehiculo(idVehiculoGenerado); // <-- Seteamos la FK
            
            // 6. Insertar B (Seguro) SEGUNDO
            // (Accedemos al seguroDAO a través del vehiculoDAO inyectado)
            vehiculoDAO.seguroDAO.insertarTx(seguro, tm.getConnection());

            // 7. Commit
            tm.commit();
            
        } catch (Exception e) {
            // El TransactionManager.close() llama a rollback() automáticamente
            System.err.println("La transacción de inserción (Vehiculo/Seguro) falló. Rollback asegurado.");
            // Relanzamos la excepción para que el MenuHandler la muestre al usuario
            throw new Exception("Error en la transacción: " + e.getMessage()); 
        }
    }
    
    /**
     * Actualiza el Vehiculo (A) y el Seguro (B) de forma ATÓMICA.
     * @param vehiculo La entidad con los datos actualizados.
     * @throws Exception Si falla la validación o la persistencia.
     */
    @Override
    public void actualizar(Vehiculo vehiculo) throws Exception { 
        validar(vehiculo);
        
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();
            
            // 1. Actualizar A (Vehiculo)
            vehiculoDAO.actualizarTx(vehiculo, tm.getConnection());
            
            // 2. Si tiene Seguro, actualizar B (Seguro)
            if (vehiculo.getSeguro() != null) {
                SeguroVehicular seguro = vehiculo.getSeguro();
                seguroService.validar(seguro);
                
                // (Falta lógica de unicidad de póliza si cambió)
                
                // Si el seguro no tiene ID (es nuevo), no se puede actualizar
                if (seguro.getId() == 0) {
                     throw new IllegalArgumentException("No se puede crear un nuevo seguro en una actualización. La relación 1:1 es fija.");
                } else {
                    // Si ya tiene ID, solo lo actualizamos
                    vehiculoDAO.seguroDAO.actualizarTx(seguro, tm.getConnection());
                }
            }
            // NOTA: Faltaría lógica para desasociar un seguro si se elimina.

            tm.commit();
        } catch (Exception e) {
            System.err.println("La transacción de actualización falló. Rollback asegurado.");
            throw new Exception("Error en la transacción: " + e.getMessage());
        }
    }
    
    /**
     * Aplica Baja Lógica al Vehiculo (A) y al Seguro (B) de forma ATÓMICA.
     * @param id El ID de la entidad a marcar como eliminada.
     * @throws Exception Si la entidad no se encuentra o falla la persistencia.
     */
    @Override
    public void eliminar(int id) throws Exception { 
        // 1. Cargamos el objeto completo (con Eager Loading del seguro)
        Vehiculo vehiculo = vehiculoDAO.getById(id); 
        
        if (vehiculo == null) {
            throw new Exception("Vehículo con ID " + id + " no encontrado o ya eliminado.");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             TransactionManager tm = new TransactionManager(conn)) {

            tm.startTransaction();
            
            // 2. Aplicar baja lógica al Vehiculo (A)
            vehiculoDAO.eliminarTx(id, tm.getConnection());
            
            // 3. Si tiene Seguro, aplicar baja lógica al Seguro (B)
            if (vehiculo.getSeguro() != null) {
                // Obtenemos el ID del seguro que cargamos con la búsqueda Eager
                int seguroId = (int) vehiculo.getSeguro().getId();
                vehiculoDAO.seguroDAO.eliminarTx(seguroId, tm.getConnection());
            }

            tm.commit(); // Ambas entidades (A y B) se marcan como 'eliminado=true'
            
        } catch (Exception e) {
            System.err.println("La transacción de eliminación (baja lógica) falló. Rollback asegurado.");
            throw new Exception("Error en la transacción: " + e.getMessage());
        }
    }
    
    // =================================================================
    // OPERACIONES DE LECTURA (DELEGAN A DAO)
    // =================================================================
    
    /**
     * Recupera un vehículo por su ID (con Eager Loading del Seguro).
     * @param id El ID de la entidad a buscar.
     * @return El vehículo encontrado, or null.
     * @throws Exception Si falla la consulta.
     */
    @Override
    public Vehiculo getById(int id) throws Exception { 
        // El DAO se encarga del LEFT JOIN para cargar el Seguro (Eager Load)
        return vehiculoDAO.getById(id);
    }
    
    /**
     * Recupera todos los vehículos activos (con Eager Loading de Seguros).
     * @return Una lista de vehículos activos.
     * @throws Exception Si falla la consulta.
     */
    @Override
    public List<Vehiculo> getAll() throws Exception { 
        // El DAO se encarga del LEFT JOIN
        return vehiculoDAO.getAll();
    }
    
    /**
     * Búsqueda por campo clave (Dominio).
     * @param dominio El dominio a buscar.
     * @return El Vehiculo (mapeo simple) o null.
     * @throws Exception Si falla la consulta.
     */
    public Vehiculo buscarPorDominio(String dominio) throws Exception {
        // Usa null en la conexión porque esta operación de lectura no es parte de una Tx
        return vehiculoDAO.buscarPorCampoClave(dominio.toUpperCase(), null);
    }
}