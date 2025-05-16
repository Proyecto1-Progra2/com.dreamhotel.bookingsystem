package domain;

public class Habitacion {

    private String estado, estilo, numeroHabitacion;
    private int precio;

    public Habitacion(String estado, String estilo, String numeroHabitacion, int precio) {
        this.estado = estado;
        this.estilo = estilo;
        this.numeroHabitacion = numeroHabitacion;
        this.precio = precio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstilo() {
        return estilo;
    }

    public void setEstilo(String estilo) {
        this.estilo = estilo;
    }

    public String getNumeroHabitacion() {
        return numeroHabitacion;
    }

    public void setNumeroHabitacion(String numeroHabitacion) {
        this.numeroHabitacion = numeroHabitacion;
    }

    public int getPrecio() {
        return precio;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "-"+this.estado+"-"+this.estilo+"-"+this.numeroHabitacion+"-"+this.precio;
    }
}
