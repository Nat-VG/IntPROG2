package entities;

public class Vehiculo extends Base {
    
    private String dominio;
    private String marca;
    private String modelo;
    private int anio;
    private String nroChasis;
    
    private SeguroVehicular seguro;

    public Vehiculo() {}

    public Vehiculo(long id, boolean eliminado, String dominio, String marca, String modelo, int anio, String nroChasis) {
        super(id, eliminado);
        this.dominio = dominio;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.nroChasis = nroChasis;
    }

    // --- Getters y Setters ---

    public String getDominio() {
        return dominio;
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public String getNroChasis() {
        return nroChasis;
    }

    public void setNroChasis(String nroChasis) {
        this.nroChasis = nroChasis;
    }

    public SeguroVehicular getSeguro() {
        return seguro;
    }

    public void setSeguro(SeguroVehicular seguro) {
        this.seguro = seguro;
    }

    /**
     * toString() corregido sin acentos.
     */
    @Override
    public String toString() {
        String infoSeguro;
        if (seguro != null) {
            infoSeguro = "seguro=Poliza Nro. " + seguro.getNroPoliza() + " (ID: " + seguro.getId() + ")";
        } else {
            infoSeguro = "seguro=sin seguro asociado";
        }
        
        return "Vehiculo{" +
                "id=" + getId() +
                ", dominio='" + dominio + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", ano=" + anio + // Corregido a 'ano'
                ", nroChasis='" + nroChasis + '\'' +
                ", " + infoSeguro +
                ", eliminado=" + isEliminado() +
                '}';
    }
}