package entities;

// Clase base que usan todas nuestras entidades para no repetir código
// Es abstracta porque no queremos que se pueda crear un objeto "Base" directamente
// Solo la usamos para heredar
public abstract class Base {
    private long id;           // Este id lo genera automáticamente la base de datos
    private boolean eliminado; // Para la baja lógica - no borramos de verdad

    // Constructor vacío - lo necesitan algunos frameworks
    public Base() {}

    // Constructor completo - para cuando cargamos datos desde la base
    public Base(long id, boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }

    // GETTERS Y SETTERS - forma segura de acceder a los atributos privados
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isEliminado() {
        return eliminado;
    }

    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }

    // toString() nos ayuda a debuggear - muestra el objeto como texto
    @Override
    public String toString() {
        return "id=" + id + ", eliminado=" + eliminado;
    }
}