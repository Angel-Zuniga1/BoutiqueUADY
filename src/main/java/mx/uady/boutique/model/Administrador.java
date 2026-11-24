package mx.uady.boutique.model;

/** Cuenta del panel de administración (tabla administradores): llave, principal o sucursal. */
public class Administrador {

    public static final String ROL_LLAVE = "llave";
    public static final String ROL_PRINCIPAL = "principal";
    public static final String ROL_SUCURSAL = "sucursal";

    private int idAdmin;
    private String nombre;
    private String correo;
    private transient String passwordHash;
    private String rol;
    private Integer idSucursal;
    private String sucursal;
    private boolean activo;

    public Administrador() {
    }

    public int getIdAdmin() { return idAdmin; }
    public void setIdAdmin(int idAdmin) { this.idAdmin = idAdmin; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Integer getIdSucursal() { return idSucursal; }
    public void setIdSucursal(Integer idSucursal) { this.idSucursal = idSucursal; }

    public String getSucursal() { return sucursal; }
    public void setSucursal(String sucursal) { this.sucursal = sucursal; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean esPrincipal() { return ROL_PRINCIPAL.equals(rol); }
}
