package itcareservas.modelo;

import itcareservasmodelo.enums.TipoAula;
import java.io.Serializable;

public class Aula implements Serializable {
    private String id;
    private String nombre;
    private int capacidad;
    private TipoAula tipo;

    public Aula(String id, String nombre, int capacidad, TipoAula tipo) {
        this.id = id;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoAula getTipo() { return tipo; }
    public int getCapacidad() { return capacidad; }
    
    // Setters
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(TipoAula tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return String.format("ID: %s, Nombre: %s, Tipo: %s, Capacidad: %d", 
                             id, nombre, tipo, capacidad);
    }
    
    public static Aula fromString(String data) {
        String[] parts = data.split(",");
        return new Aula(parts[0], parts[1], 
                        Integer.parseInt(parts[2]), TipoAula.valueOf(parts[3]));
    }
    
    public String toCSV() {
        return String.format("%s,%s,%d,%s", id, nombre, capacidad, tipo);
    }
}