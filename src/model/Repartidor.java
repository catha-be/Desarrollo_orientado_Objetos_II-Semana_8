package model;

public class Repartidor {

    private int id;
    private String nombre;

    // Repartidor nuevo: el ID lo asigna la BD
    public Repartidor(String nombre) {
        this(0, nombre);
    }

    public Repartidor(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Texto legible para los JComboBox (el id se conserva en el objeto)
    @Override
    public String toString() {
        return id + " - " + nombre;
    }
}
