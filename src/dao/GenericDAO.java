package dao;

import java.sql.Connection;
import java.util.List;

/**
 * Interfaz que define el contrato de persistencia genérico (CRUD) 
 * para cualquier entidad T. Se incluyen versiones transaccionales (Tx)
 * para orquestación por la capa Service.
 *
 * @param <T> El tipo de entidad con el que trabajará el DAO (ej. Vehiculo o SeguroVehicular).
 */
public interface GenericDAO<T> {
    
    // --- MÉTODOS SIN TRANSACCIÓN (Manejan su propia Connection) ---

    /**
     * Inserta una entidad (método no transaccional).
     * @param entidad La entidad a persistir.
     * @throws Exception Si falla la conexión o la inserción.
     */
    void insertar(T entidad) throws Exception;

    /**
     * Actualiza una entidad (método no transaccional).
     * @param entidad La entidad con los datos actualizados.
     * @throws Exception Si falla la conexión o la actualización.
     */
    void actualizar(T entidad) throws Exception;

    /**
     * Elimina lógicamente una entidad (método no transaccional).
     * @param id El ID de la entidad a eliminar.
     * @throws Exception Si falla la conexión o la baja lógica.
     */
    void eliminar(int id) throws Exception;
    
    /**
     * Busca una entidad por su ID (método no transaccional).
     * @param id El ID a buscar.
     * @return La entidad encontrada, o null si no existe.
     * @throws Exception Si falla la conexión o la consulta.
     */
    T getById(int id) throws Exception;
    
    /**
     * Obtiene todas las entidades activas (método no transaccional).
     * @return Una lista de entidades.
     * @throws Exception Si falla la conexión o la consulta.
     */
    List<T> getAll() throws Exception;
    
    // --- MÉTODOS TRANSACCIONALES (Requieren Connection externa del Service) ---
    
    /**
     * Persiste la entidad dentro de una transacción activa.
     * @param entity La entidad a persistir.
     * @param conn La conexión transaccional activa (provista por el Service).
     * @return El ID generado para la entidad.
     * @throws Exception Si falla la inserción.
     */
    long insertarTx(T entity, Connection conn) throws Exception;

    /**
     * Actualiza la entidad dentro de una transacción activa.
     * @param entity La entidad con datos actualizados.
     * @param conn La conexión transaccional activa.
     * @throws Exception Si falla la actualización.
     */
    void actualizarTx(T entity, Connection conn) throws Exception;
    
    /**
     * Aplica la baja lógica de la entidad dentro de una transacción activa.
     * @param id El ID de la entidad a eliminar.
     * @param conn La conexión transaccional activa.
     * @throws Exception Si falla la baja lógica.
     */
    void eliminarTx(int id, Connection conn) throws Exception;
    
    /**
     * Busca la entidad por un campo clave de unicidad (ej. Dominio o Nro. Póliza).
     * @param valor El valor del campo clave a buscar (ej. "ABC123").
     * @param conn La conexión transaccional activa (o null si es una búsqueda simple).
     * @return La entidad encontrada, o null si no existe.
     * @throws Exception Si falla la consulta.
     */
    T buscarPorCampoClave(String valor, Connection conn) throws Exception;
}