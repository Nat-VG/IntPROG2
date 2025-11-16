package entities;

import java.time.LocalDate;

/**
 * Clase SeguroVehicular (Clase B).
 * CORREGIDA: Se elimina el campo 'idVehiculo' para garantizar
 * la unidireccionalidad pura (A -> B).
 */
public class SeguroVehicular extends Base {
    
    private String aseguradora;
    private String nroPoliza;
    private Cobertura cobertura;
    private LocalDate vencimiento;
    
    // ELIMINADO: private long idVehiculo; 
    // La FK se maneja solo en la capa DAO/Service

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
     * @param vencimiento La fecha de vencimiento.
     */
    public void setVencimiento(LocalDate vencimiento) {
        this.vencimiento = vencimiento;
    }

    // ELIMINADOS: getIdVehiculo(), setIdVehiculo()

    @Override
    public String toString() {
        return "SeguroVehicular{" +
                "id=" + getId() +
                ", aseguradora='" + aseguradora + '\'' +
                ", nroPoliza='" + nroPoliza + '\'' +
                ", cobertura=" + cobertura +
                ", vencimiento=" + vencimiento +
                ", eliminado=" + isEliminado() +
                '}';
    }
}