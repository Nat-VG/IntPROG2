package entities;

/**
 * Clase Vehiculo (Clase A).
 * Entidad principal que representa un vehículo en el sistema.
 * Extiende de Base para heredar 'id' y 'eliminado' (baja lógica).
 * Contiene la referencia unidireccional 1:1 al SeguroVehicular (A -> B).
 *
 * @author [Tu Nombre/Grupo Aquí]
 */
public class Vehiculo extends Base {
    
    // --- Atributos Específicos ---
    private String dominio;
    private String marca;
    private String modelo;
    private int anio;
    private String nroChasis;
    
    /**
     * Relación 1:1 Unidireccional (A -> B).
     * El Vehículo "conoce" a su Seguro.
     * El Seguro NO conoce a su Vehículo (en el modelo de objetos).
     * Esta referencia se carga usando Eager Loading (LEFT JOIN) en el DAO.
     */
    private SeguroVehicular seguro;

    /**
     * Constructor vacío (default).
     */
    public Vehiculo() {
        super(); // Llama al constructor de Base
    }

    /**
     * Constructor completo (usado para mapear desde la BD).
     *
     * @param id El ID de la entidad.
     * @param eliminado El estado de baja lógica.
     * @param dominio La patente/dominio.
     * @param marca La marca.
     * @param modelo El modelo.
     * @param anio El año de fabricación.
     * @param nroChasis El número de chasis.
     */
    public Vehiculo(long id, boolean eliminado, String dominio, String marca, String modelo, int anio, String nroChasis) {
        super(id, eliminado); // Llama al constructor del padre (Base)
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

    /**
     * Getter para la relación 1:1.
     * @return El objeto SeguroVehicular asociado (o null si no tiene).
     */
    public SeguroVehicular getSeguro() {
        return seguro;
    }

    /**
     * Setter para la relación 1:1.
     * @param seguro El objeto SeguroVehicular a asociar.
     */
    public void setSeguro(SeguroVehicular seguro) {
        this.seguro = seguro;
    }

    /**
     * Genera una representación en String del Vehículo y su Seguro asociado.
     * Demuestra la Carga Eager (si el DAO funcionó, 'seguro' no será null).
     *
     * @return Un String con los datos del vehículo y su seguro.
     */
    @Override
    public String toString() {
        // Lógica para mostrar el seguro (si está cargado) o "sin seguro"
        String infoSeguro;
        if (seguro != null) {
            infoSeguro = "seguro=Póliza N° " + seguro.getNroPoliza() + " (ID: " + seguro.getId() + ")";
        } else {
            infoSeguro = "seguro=sin seguro asociado";
        }
        
        return "Vehiculo{" +
                "id=" + getId() +
                ", dominio='" + dominio + '\'' +
                ", marca='" + marca + '\'' +
                ", modelo='" + modelo + '\'' +
                ", anio=" + anio +
                ", nroChasis='" + nroChasis + '\'' +
                ", " + infoSeguro +
                ", eliminado=" + isEliminado() +
                '}';
    }
}