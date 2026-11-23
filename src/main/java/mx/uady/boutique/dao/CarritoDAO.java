package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.ItemCarrito;

/**
 * Acceso a las tablas {@code carritos} y {@code detalle_carrito}.
 * El nombre, precio e imagen de cada línea se leen siempre de {@code productos}
 * (el navegador solo manda id, talla y cantidad que se toman en cuenta).
 */
public class CarritoDAO {

    private static final String SQL_CARRITOS = "CREATE TABLE IF NOT EXISTS carritos ("
            + "id_carrito INT AUTO_INCREMENT PRIMARY KEY, "
            + "id_usuario INT NOT NULL UNIQUE, "
            + "FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static final String SQL_DETALLE = "CREATE TABLE IF NOT EXISTS detalle_carrito ("
            + "id_carrito INT NOT NULL, "
            + "id_producto INT NOT NULL, "
            + "talla VARCHAR(10) NOT NULL DEFAULT '', "
            + "cantidad INT NOT NULL, "
            + "orden INT NOT NULL DEFAULT 0, "
            + "PRIMARY KEY (id_carrito, id_producto, talla), "
            + "FOREIGN KEY (id_carrito) REFERENCES carritos(id_carrito) ON DELETE CASCADE, "
            + "FOREIGN KEY (id_producto) REFERENCES productos(id_producto)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    /** Crea las tablas del carrito si no existen (el script SQL ya las incluye). */
    public void asegurarTabla() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement()) {
            st.execute(SQL_CARRITOS);
            st.execute(SQL_DETALLE);
        }
    }

    /** Devuelve el carrito del usuario en el orden en que se guardó, con precios de la base. */
    public List<ItemCarrito> listar(int idUsuario) throws SQLException {
        String sql = "SELECT p.id_producto, d.talla, p.nombre, p.precio, p.imagen, d.cantidad "
                + "FROM carritos c "
                + "JOIN detalle_carrito d ON d.id_carrito = c.id_carrito "
                + "JOIN productos p ON p.id_producto = d.id_producto "
                + "WHERE c.id_usuario = ? ORDER BY d.orden";
        List<ItemCarrito> items = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new ItemCarrito(
                            String.valueOf(rs.getInt("id_producto")),
                            rs.getString("talla"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getString("imagen"),
                            rs.getInt("cantidad")));
                }
            }
        }
        return items;
    }

    /**
     * Reemplaza el carrito completo del usuario dentro de una transacción.
     * Las líneas cuyo producto no exista o esté inactivo se ignoran.
     */
    public void reemplazar(int idUsuario, List<ItemCarrito> items) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int idCarrito = obtenerOCrearCarrito(con, idUsuario);
                try (PreparedStatement borrar =
                             con.prepareStatement("DELETE FROM detalle_carrito WHERE id_carrito = ?")) {
                    borrar.setInt(1, idCarrito);
                    borrar.executeUpdate();
                }
                String sql = "INSERT INTO detalle_carrito (id_carrito, id_producto, talla, cantidad, orden) "
                        + "SELECT ?, id_producto, ?, ?, ? FROM productos WHERE id_producto = ? AND activo = 1";
                try (PreparedStatement insertar = con.prepareStatement(sql)) {
                    int orden = 0;
                    for (ItemCarrito item : items) {
                        int idProducto;
                        try {
                            idProducto = Integer.parseInt(item.getId());
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        insertar.setInt(1, idCarrito);
                        insertar.setString(2, item.getTalla());
                        insertar.setInt(3, item.getCantidad());
                        insertar.setInt(4, orden);
                        insertar.setInt(5, idProducto);
                        insertar.executeUpdate();
                        orden++;
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private int obtenerOCrearCarrito(Connection con, int idUsuario) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id_carrito FROM carritos WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO carritos (id_usuario) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
