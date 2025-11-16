package service;

import java.util.List;

/**
 * Interfaz genérica que define el contrato de la capa de Servicio.
 * Esta capa se encarga de las validaciones de negocio y la orquestación transaccional.
 *
 * @param <T> El tipo de entidad (ej. Vehiculo o SeguroVehicular).
 */
public interface GenericService<T> {
    
    void insertar(T entidad) throws Exception;
    void actualizar(T entidad) throws Exception;
    void eliminar(int id) throws Exception;
    T getById(int id) throws Exception;
    List<T> getAll() throws Exception;
}