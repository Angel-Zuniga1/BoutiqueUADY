package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import mx.uady.boutique.model.Usuario;

/**
 * Acceso a la tabla {@code usuarios}. Todas las consultas usan {@link PreparedStatement}.
 */
public class UsuarioDAO {

    /**
     * Indica si ya existe un usuario con ese correo.
     */
    public boolean existeCorreo(String correo) throws SQLException {
        String sql = "SELECT id_usuario FROM usuarios WHERE correo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Inserta un nuevo usuario.
     *
     * @return {@code true} si se insertó el registro
     */
    public boolean insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nombre, apellidos, correo, password_hash, telefono) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCorreo());
            ps.setString(4, usuario.getPasswordHash());
            ps.setString(5, usuario.getTelefono());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Busca un usuario por correo.
     *
     * @return el usuario (id, nombre y hash) si existe exactamente una coincidencia; si no, {@code null}
     */
    public Usuario buscarPorCorreo(String correo) throws SQLException {
        String sql = "SELECT id_usuario, nombre, password_hash FROM usuarios WHERE correo = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setCorreo(correo);
                // La Beta 1.0 exigía exactamente una coincidencia.
                return rs.next() ? null : u;
            }
        }
    }
}
