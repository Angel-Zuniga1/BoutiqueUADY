package mx.uady.boutique.model;

/**
 * Una línea del carrito de un usuario (tabla {@code carrito_usuario}).
 * Los nombres de los campos son los mismos que usa el JSON que intercambia carrito.js
 * ({@code id, talla, nombre, precio, imagen, cantidad}).
 */
public class ItemCarrito {

    private String id;
    private String talla;
    private String nombre;
    private double precio;
    private String imagen;
    private int cantidad;

    public ItemCarrito() {
    }

    public ItemCarrito(String id, String talla, String nombre, double precio, String imagen, int cantidad) {
        this.id = id;
        this.talla = talla;
        this.nombre = nombre;
        this.precio = precio;
        this.imagen = imagen;
        this.cantidad = cantidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
