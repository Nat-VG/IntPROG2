package entities;

import java.time.LocalDate;

/**
 * Clase SeguroVehicular (Clase B).
 * Esta versión incluye el campo 'idVehiculo' necesario para
 * que el DAO inserte la Clave Foránea (FK) correctamente.
 */
public class SeguroVehicular extends Base {
    
    private String aseguradora;
    private String nroPoliza;
    private Cobertura cobertura;
    private LocalDate vencimiento;
    
    /**
     * Campo para almacenar la FK (id del Vehículo) temporalmente
     * antes de la inserción en el DAO.
     */
    private long idVehiculo;

    public SeguroVehicular() {}

    public SeguroVehicular(long id, boolean eliminado, String aseguradora, String nroPoliza, Cobertura cobertura, LocalDate vencimiento) {
        super(id, eliminado);
        this.aseguradora = aseguradora;
        this.nroPoliza = nroPoliza;
        this.cobertura = cobertura;
        this.vencimiento = vencimiento;
    }

    // --- Getters y Setters ---

    public String getAseguradora() {
        return aseguradora;
    }

    public void setAseguradora(String aseguradora) {
        this.aseguradora = aseguradora;
    }

    public String getNroPoliza() {
        return nroPoliza;
    }

    public void setNroPoliza(String nroPoliza) {
        this.nroPoliza = nroPoliza;
    }

    public Cobertura getCobertura() {
        return cobertura;
    }

    public void setCobertura(Cobertura cobertura) {
        this.cobertura = cobertura;
    }

    public LocalDate getVencimiento() {
        return vencimiento;
    }

    /**
     * Setter para el vencimiento (LocalDate).
     * Este es el método que tu DAO está buscando.
     * @param vencimiento La fecha de vencimiento.
     */
    public void setVencimiento(LocalDate vencimiento) {
        this.vencimiento = vencimiento;
    }

    /**
     * Getter/Setter para la FK (idVehiculo).
     * Esto soluciona el error de INSERT.
     */
    public long getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(long idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    @Override
    public String toString() {
        return "SeguroVehicular{" +
                "id=" + getId() +
                ", aseguradora='" + aseguradora + '\'' +
                ", nroPoliza='" + nroPoliza + '\'' +
                ", cobertura=" + cobertura +
                ", vencimiento=" + vencimiento +
                ", idVehiculo=" + idVehiculo + // Mostramos la FK
                ", eliminado=" + isEliminado() +
                '}';
    }
}