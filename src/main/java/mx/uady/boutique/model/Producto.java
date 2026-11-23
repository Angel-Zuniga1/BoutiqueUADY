package mx.uady.boutique.model;

import java.util.ArrayList;
import java.util.List;

public class Producto {

    private int id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String imagen;
    private int idCategoria;
    private String categoria;
    private List<Disponibilidad> disponibilidad = new ArrayList<>();

    public Producto() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public List<Disponibilidad> getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(List<Disponibilidad> disponibilidad) { this.disponibilidad = disponibilidad; }
}
