package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.Administrador;

/** Acceso a la tabla administradores (cuentas llave, principal y de sucursal). */
public class AdministradorDAO {

    private static final String SELECT = "SELECT a.id_admin, a.nombre, a.correo, a.password_hash, a.rol, "
            + "a.id_sucursal, a.activo, s.nombre AS sucursal "
            + "FROM administradores a LEFT JOIN sucursales s ON s.id_sucursal = a.id_sucursal ";

    public Administrador buscarPorCorreo(String correo) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT + "WHERE a.correo = ?")) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Cuentas principal y de sucursal (la llave no se administra desde el panel). */
    public List<Administrador> listar() throws SQLException {
        List<Administrador> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT
                     + "WHERE a.rol IN ('principal', 'sucursal') ORDER BY a.rol = 'principal' DESC, a.id_admin");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public void crearDeSucursal(String nombre, String correo, String hash, int idSucursal) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO administradores (nombre, correo, password_hash, rol, id_sucursal) VALUES (?, ?, ?, 'sucursal', ?)")) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, hash);
            ps.setInt(4, idSucursal);
            ps.executeUpdate();
        }
    }

    public void cambiarActivo(int idAdmin, boolean activo) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE administradores SET activo = ? WHERE id_admin = ? AND rol = 'sucursal'")) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, idAdmin);
            ps.executeUpdate();
        }
    }

    public void cambiarPassword(int idAdmin, String hash) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE administradores SET password_hash = ? WHERE id_admin = ? AND rol = 'sucursal'")) {
            ps.setString(1, hash);
            ps.setInt(2, idAdmin);
            ps.executeUpdate();
        }
    }

    public int contarActivosDeSucursal() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM administradores WHERE rol = 'sucursal' AND activo = 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Nombres de los administradores activos de una sucursal, separados por coma. */
    public String nombresDeSucursal(int idSucursal) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT GROUP_CONCAT(nombre SEPARATOR ', ') FROM administradores "
                     + "WHERE rol = 'sucursal' AND activo = 1 AND id_sucursal = ?")) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getString(1) != null ? rs.getString(1) : "";
            }
        }
    }

    private Administrador mapear(ResultSet rs) throws SQLException {
        Administrador a = new Administrador();
        a.setIdAdmin(rs.getInt("id_admin"));
        a.setNombre(rs.getString("nombre"));
        a.setCorreo(rs.getString("correo"));
        a.setPasswordHash(rs.getString("password_hash"));
        a.setRol(rs.getString("rol"));
        int idSuc = rs.getInt("id_sucursal");
        a.setIdSucursal(rs.wasNull() ? null : idSuc);
        a.setActivo(rs.getInt("activo") == 1);
        a.setSucursal(rs.getString("sucursal"));
        return a;
    }
}
