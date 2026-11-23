package mx.uady.boutique.model;

/**
 * Representa un registro de la tabla {@code usuarios}.
 */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String apellidos;
    private String correo;
    private String passwordHash;
    private String telefono;

    public Usuario() {
    }

    public Usuario(int idUsuario, String nombre, String apellidos, String correo,
                   String passwordHash, String telefono) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.telefono = telefono;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
