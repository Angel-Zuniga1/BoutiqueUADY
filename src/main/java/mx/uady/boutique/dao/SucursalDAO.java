package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.Sucursal;

public class SucursalDAO {

    public List<Sucursal> listarActivas() throws SQLException {
        return listar(true);
    }

    /** Todas las sucursales (activas e inactivas) para el administrador principal. */
    public List<Sucursal> listarTodas() throws SQLException {
        return listar(false);
    }

    private List<Sucursal> listar(boolean soloActivas) throws SQLException {
        List<Sucursal> lista = new ArrayList<>();
        String sql = "SELECT id_sucursal, nombre, activa FROM sucursales "
                + (soloActivas ? "WHERE activa = 1 " : "") + "ORDER BY id_sucursal";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Sucursal s = new Sucursal(rs.getInt("id_sucursal"), rs.getString("nombre"));
                s.setActiva(rs.getInt("activa") == 1);
                lista.add(s);
            }
        }
        return lista;
    }

    public String nombreDe(int idSucursal) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT nombre FROM sucursales WHERE id_sucursal = ?")) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public boolean existeNombre(String nombre) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT 1 FROM sucursales WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void crear(String nombre) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("INSERT INTO sucursales (nombre, activa) VALUES (?, 1)")) {
            ps.setString(1, nombre);
            ps.executeUpdate();
        }
    }

    public void cambiarActiva(int idSucursal, boolean activa) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE sucursales SET activa = ? WHERE id_sucursal = ?")) {
            ps.setInt(1, activa ? 1 : 0);
            ps.setInt(2, idSucursal);
            ps.executeUpdate();
        }
    }
}
