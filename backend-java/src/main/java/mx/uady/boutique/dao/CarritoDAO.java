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
 * Acceso a la tabla {@code carrito_usuario} (carrito guardado por usuario).
 * La tabla se crea sola si no existe, igual que hacía carrito_api.php.
 */
public class CarritoDAO {

    private static final String SQL_TABLA = "CREATE TABLE IF NOT EXISTS carrito_usuario ("
            + "id_usuario INT NOT NULL, "
            + "id_producto VARCHAR(20) NOT NULL, "
            + "talla VARCHAR(10) NOT NULL DEFAULT '', "
            + "nombre VARCHAR(120) NOT NULL, "
            + "precio DECIMAL(10,2) NOT NULL, "
            + "imagen VARCHAR(255) NOT NULL DEFAULT '', "
            + "cantidad INT NOT NULL, "
            + "orden INT NOT NULL DEFAULT 0, "
            + "PRIMARY KEY (id_usuario, id_producto, talla)"
            + "%s"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

    private static final String LLAVE_FORANEA =
            ", FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE";

    /**
     * Crea la tabla con llave foránea; si eso falla, la crea sin ella.
     */
    public void asegurarTabla() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             Statement st = con.createStatement()) {
            try {
                st.execute(String.format(SQL_TABLA, LLAVE_FORANEA));
            } catch (SQLException e) {
                st.execute(String.format(SQL_TABLA, ""));
            }
        }
    }

    /**
     * Devuelve el carrito del usuario en el orden en que se guardó.
     */
    public List<ItemCarrito> listar(int idUsuario) throws SQLException {
        String sql = "SELECT id_producto, talla, nombre, precio, imagen, cantidad "
                + "FROM carrito_usuario WHERE id_usuario = ? ORDER BY orden";
        List<ItemCarrito> items = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new ItemCarrito(
                            rs.getString("id_producto"),
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
     * Reemplaza el carrito completo del usuario dentro de una transacción:
     * si algo falla no se pierde el carrito anterior.
     */
    public void reemplazar(int idUsuario, List<ItemCarrito> items) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement borrar =
                             con.prepareStatement("DELETE FROM carrito_usuario WHERE id_usuario = ?")) {
                    borrar.setInt(1, idUsuario);
                    borrar.executeUpdate();
                }
                if (!items.isEmpty()) {
                    String sql = "INSERT INTO carrito_usuario "
                            + "(id_usuario, id_producto, talla, nombre, precio, imagen, cantidad, orden) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement insertar = con.prepareStatement(sql)) {
                        int orden = 0;
                        for (ItemCarrito item : items) {
                            insertar.setInt(1, idUsuario);
                            insertar.setString(2, item.getId());
                            insertar.setString(3, item.getTalla());
                            insertar.setString(4, item.getNombre());
                            insertar.setDouble(5, item.getPrecio());
                            insertar.setString(6, item.getImagen());
                            insertar.setInt(7, item.getCantidad());
                            insertar.setInt(8, orden);
                            insertar.executeUpdate();
                            orden++;
                        }
                    }
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
