package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.Producto;

public class ProductoDAO {

    private static final String SELECT = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.imagen, "
            + "p.id_categoria, c.nombre AS categoria "
            + "FROM productos p JOIN categorias c ON c.id_categoria = p.id_categoria "
            + "WHERE p.activo = 1";

    public List<Producto> listarActivos() throws SQLException {
        List<Producto> r = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT + " ORDER BY p.id_producto");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto();
                p.setId(rs.getInt(1));
                p.setNombre(rs.getString(2));
                p.setDescripcion(rs.getString(3));
                p.setPrecio(rs.getDouble(4));
                p.setImagen(rs.getString(5));
                p.setIdCategoria(rs.getInt(6));
                p.setCategoria(rs.getString(7));
                r.add(p);
            }
        }
        return r;
    }

    public Producto buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SELECT + " AND p.id_producto = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Producto p = new Producto();
                    p.setId(rs.getInt(1));
                    p.setNombre(rs.getString(2));
                    p.setDescripcion(rs.getString(3));
                    p.setPrecio(rs.getDouble(4));
                    p.setImagen(rs.getString(5));
                    p.setIdCategoria(rs.getInt(6));
                    p.setCategoria(rs.getString(7));
                    return p;
                }
            }
        }
        return null;
    }

    /** Cuenta los productos activos. (Nadie la usa todavía.) */
    public int contar() throws SQLException {
        int x = 0;
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM productos WHERE activo = 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                x = rs.getInt(1);
            }
        }
        return x;
    }
}
