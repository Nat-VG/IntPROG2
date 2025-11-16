package service;

import config.DatabaseConnection;
import dao.SeguroVehicularDAO;
import entities.SeguroVehicular;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para la entidad SeguroVehicular (Clase B).
 * Implementa las validaciones de negocio (Reglas de Negocio).
 *
 * @author [Tu Nombre/Grupo Aquí]
 */
public class SeguroVehicularServiceImpl implements GenericService<SeguroVehicular> {

    // El DAO se mantiene privado. La inyección de dependencias
    // se maneja en AppMenu.
    private final SeguroVehicularDAO seguroDAO;

    /**
     * Constructor para Inyección de Dependencias.
     * @param seguroDAO El DAO de SeguroVehicular.
     */
    public SeguroVehicularServiceImpl(SeguroVehicularDAO seguroDAO) {
        this.seguroDAO = seguroDAO;
    }

    // =================================================================
    // MÉTODOS DE VALIDACIÓN (Lógica de Negocio)
    // =================================================================

    /**
     * Valida que los campos del Seguro sean correctos (Regla de Negocio).
     * @param seguro El objeto SeguroVehicular a validar.
     * @throws IllegalArgumentException Si algún campo es inválido.
     */
    public void validar(SeguroVehicular seguro) throws IllegalArgumentException {
        if (seguro == null) {
            throw new IllegalArgumentException("El objeto SeguroVehicular no puede ser nulo.");
        }
        if (seguro.getNroPoliza() == null || seguro.getNroPoliza().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de póliza es obligatorio.");
        }
        if (seguro.getAseguradora() == null || seguro.getAseguradora().trim().isEmpty()) {
            throw new IllegalArgumentException("La aseguradora es obligatoria.");
        }
        if (seguro.getVencimiento() == null || seguro.getVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de vencimiento es inválida (no puede ser anterior a hoy).");
        }
    }

    /**
     * Valida la unicidad de la póliza (Regla de Negocio).
     * @param nroPoliza La póliza a verificar.
     * @param conn La conexión transaccional (o null si es una conexión simple).
     * @throws Exception Si la póliza ya existe o si falla la consulta.
     */
    public void validarUnicidadPoliza(String nroPoliza, Connection conn) throws Exception {
        if (seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), conn) != null) {
            throw new IllegalArgumentException("Ya existe un seguro activo con el número de póliza: " + nroPoliza);
        }
    }

    /**
     * Búsqueda por campo clave (Nro. Póliza).
     * Método público para que MenuHandler lo pueda usar.
     * @param nroPoliza El número de póliza a buscar.
     * @return El seguro encontrado, o null.
     * @throws Exception Si falla la consulta.
     */
    public SeguroVehicular buscarPorPoliza(String nroPoliza) throws Exception {
        // Usa 'null' para la conexión, ya que esta operación de lectura no es parte de una Tx
        return seguroDAO.buscarPorCampoClave(nroPoliza.toUpperCase(), null);
    }

    // =================================================================
    // MÉTODOS CRUD (Implementación de GenericService)
    // =================================================================

    /**
     * Inserta un seguro de forma independiente (no como parte de la Tx 1:1).
     * @param seguro La entidad a persistir (idVehiculo DEBE estar seteado).
     * @throws Exception Si falla la validación o la persistencia.
     */
    @Override
    public void insertar(SeguroVehicular seguro) throws Exception {
        validar(seguro);
        
        // Validación de unicidad usando una conexión propia
        try(Connection conn = DatabaseConnection.getConnection()) {
            validarUnicidadPoliza(seguro.getNroPoliza(), conn);
        }
        
        // La FK (idVehiculo) es NOT NULL en la base de datos.
        // Este método fallará si 'seguro.idVehiculo' no fue seteado 
        // en el MenuHandler (Opción 8).
        if (seguro.getIdVehiculo() <= 0) {
             throw new IllegalArgumentException("No se puede crear un seguro independiente sin un ID de Vehículo (debido a la FK NOT NULL).");
        }
        
        seguroDAO.insertar(seguro);
    }

    /**
     * Actualiza un seguro de forma independiente.
     * @param seguro La entidad con los datos actualizados.
     * @throws Exception Si falla la validación o la persistencia.
     */
    @Override
    public void actualizar(SeguroVehicular seguro) throws Exception {
        validar(seguro);
        // (Falta lógica de unicidad de póliza si cambió)
        seguroDAO.actualizar(seguro);
    }
    
    /**
     * Elimina (baja lógica) un seguro de forma independiente.
     * @param id El ID de la entidad a marcar como eliminada.
     * @throws Exception Si la entidad no se encuentra o falla la persistencia.
     */
    @Override
    public void eliminar(int id) throws Exception {
        // (Se debería validar que no esté asociado a un vehículo activo antes de eliminar)
        seguroDAO.eliminar(id);
    }

    /**
     * Recupera un seguro por su ID.
     * @param id El ID de la entidad a buscar.
     * @return La entidad encontrada, or null.
     * @throws Exception Si falla la consulta.
     */
    @Override
    public SeguroVehicular getById(int id) throws Exception {
        return seguroDAO.getById(id);
    }

    /**
     * Recupera todos los seguros activos.
     * @return Una lista de seguros activos.
     * @throws Exception Si falla la consulta.
     */
    @Override
    public List<SeguroVehicular> getAll() throws Exception {
        return seguroDAO.getAll();
    }
}