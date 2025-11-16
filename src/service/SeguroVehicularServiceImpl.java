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

    @Override
    public void insertar(SeguroVehicular seguro) throws Exception {
        validar(seguro);
        
        try(Connection conn = DatabaseConnection.getConnection()) {
            validarUnicidadPoliza(seguro.getNroPoliza(), conn);
        }
        
        if (seguro.getIdVehiculo() <= 0) {
             throw new IllegalArgumentException("No se puede crear un seguro independiente sin un ID de Vehiculo (debido a la FK NOT NULL).");
        }
        
        seguroDAO.insertar(seguro);
    }

    @Override
    public void actualizar(SeguroVehicular seguro) throws Exception {
        validar(seguro);
        seguroDAO.actualizar(seguro);
    }
    
    @Override
    public void eliminar(int id) throws Exception {
        seguroDAO.eliminar(id);
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