package mx.uady.boutique.model;

public class Disponibilidad {

    private transient int idProducto;
    private int idSucursal;
    private String sucursal;
    private String talla;
    private int existencias;

    public Disponibilidad() {
    }

    public Disponibilidad(int idProducto, int idSucursal, String sucursal, String talla, int existencias) {
        this.idProducto = idProducto;
        this.idSucursal = idSucursal;
        this.sucursal = sucursal;
        this.talla = talla;
        this.existencias = existencias;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getIdSucursal() { return idSucursal; }
    public void setIdSucursal(int idSucursal) { this.idSucursal = idSucursal; }

    public String getSucursal() { return sucursal; }
    public void setSucursal(String sucursal) { this.sucursal = sucursal; }

    public String getTalla() { return talla; }
    public void setTalla(String talla) { this.talla = talla; }

    public int getExistencias() { return existencias; }
    public void setExistencias(int existencias) { this.existencias = existencias; }
}
