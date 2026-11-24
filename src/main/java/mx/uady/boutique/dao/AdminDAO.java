package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.uady.boutique.model.ResumenVentas;

/**
 * Consultas y cambios del panel de administración por sucursal:
 * inventario, productos de la sucursal, pedidos con cliente y reportes de ventas.
 * Los resultados se devuelven como mapas listos para convertirse en JSON.
 */
public class AdminDAO {

    public static final String[] TALLAS = {"XS", "S", "M", "L", "XL", "2XL"};

    // ------------------------------------------------------------ reportes

    /** Pedidos por estado y ventas (sin cancelados). {@code idSucursal = 0} = todas. */
    public ResumenVentas resumenVentas(int idSucursal) throws SQLException {
        ResumenVentas r = new ResumenVentas();
        String sql = "SELECT estado, COUNT(*), SUM(total) FROM pedidos "
                + (idSucursal > 0 ? "WHERE id_sucursal = ? " : "") + "GROUP BY estado";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idSucursal > 0) {
                ps.setInt(1, idSucursal);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String estado = rs.getString(1);
                    int c = rs.getInt(2);
                    r.getPorEstado().put(estado, c);
                    r.setPedidos(r.getPedidos() + c);
                    if (!"cancelado".equals(estado)) {
                        r.setVentas(r.getVentas() + rs.getDouble(3));
                    }
                }
            }
        }
        return r;
    }

    public int contarAgotados(int idSucursal) throws SQLException {
        return contar("SELECT COUNT(*) FROM inventario WHERE id_sucursal = ? AND existencias = 0", idSucursal);
    }

    public int contarProductosDeSucursal(int idSucursal) throws SQLException {
        return contar("SELECT COUNT(DISTINCT i.id_producto) FROM inventario i JOIN productos p ON p.id_producto = i.id_producto "
                + "WHERE i.id_sucursal = ? AND p.activo = 1", idSucursal);
    }

    private int contar(String sql, int param) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Productos más vendidos de una sucursal (sin cancelados). */
    public List<Map<String, Object>> masVendidos(int idSucursal) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT pr.nombre, SUM(d.cantidad), SUM(d.cantidad * d.precio_unitario) "
                + "FROM detalle_pedido d JOIN pedidos p ON p.id_pedido = d.id_pedido "
                + "JOIN productos pr ON pr.id_producto = d.id_producto "
                + "WHERE p.id_sucursal = ? AND p.estado <> 'cancelado' GROUP BY pr.id_producto ORDER BY 2 DESC LIMIT 10";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("nombre", rs.getString(1));
                    m.put("unidades", rs.getInt(2));
                    m.put("importe", rs.getDouble(3));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    // ------------------------------------------------------------ inventario

    /** Inventario de una sucursal agrupado por producto: [{id, nombre, precio, activo, tallas:[{talla, existencias}]}]. */
    public List<Map<String, Object>> inventarioDe(int idSucursal) throws SQLException {
        Map<Integer, Map<String, Object>> porProducto = new LinkedHashMap<>();
        String sql = "SELECT p.id_producto, p.nombre, p.precio, p.activo, i.talla, i.existencias "
                + "FROM inventario i JOIN productos p ON p.id_producto = i.id_producto "
                + "WHERE i.id_sucursal = ? ORDER BY p.nombre, FIELD(i.talla, '', 'XS', 'S', 'M', 'L', 'XL', '2XL')";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    Map<String, Object> p = porProducto.get(id);
                    if (p == null) {
                        p = new LinkedHashMap<>();
                        p.put("id", id);
                        p.put("nombre", rs.getString(2));
                        p.put("precio", rs.getDouble(3));
                        p.put("activo", rs.getInt(4) == 1);
                        p.put("tallas", new ArrayList<Map<String, Object>>());
                        porProducto.put(id, p);
                    }
                    Map<String, Object> t = new LinkedHashMap<>();
                    t.put("talla", rs.getString(5));
                    t.put("existencias", rs.getInt(6));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> tallas = (List<Map<String, Object>>) p.get("tallas");
                    tallas.add(t);
                }
            }
        }
        return new ArrayList<>(porProducto.values());
    }

    /** Guarda varias existencias de una sucursal en una sola transacción. */
    public void guardarExistencias(int idSucursal, List<int[]> filas, List<String> tallas) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE inventario SET existencias = ? WHERE id_producto = ? AND id_sucursal = ? AND talla = ?")) {
                for (int i = 0; i < filas.size(); i++) {
                    ps.setInt(1, Math.max(0, filas.get(i)[1]));
                    ps.setInt(2, filas.get(i)[0]);
                    ps.setInt(3, idSucursal);
                    ps.setString(4, tallas.get(i));
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /** Crea o actualiza una fila de inventario. */
    public void guardarFila(int idProducto, int idSucursal, String talla, int existencias) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO inventario (id_producto, id_sucursal, talla, existencias) VALUES (?, ?, ?, ?) "
                     + "ON DUPLICATE KEY UPDATE existencias = VALUES(existencias)")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idSucursal);
            ps.setString(3, talla);
            ps.setInt(4, Math.max(0, existencias));
            ps.executeUpdate();
        }
    }

    /** Crea en cero (si no existen) las filas de inventario de un producto en una sucursal. */
    public void crearFilasEnCero(int idProducto, int idSucursal, boolean conTallas) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT IGNORE INTO inventario (id_producto, id_sucursal, talla, existencias) VALUES (?, ?, ?, 0)")) {
            for (String t : conTallas ? TALLAS : new String[]{""}) {
                ps.setInt(1, idProducto);
                ps.setInt(2, idSucursal);
                ps.setString(3, t);
                ps.executeUpdate();
            }
        }
    }

    public boolean productoEnSucursal(int idProducto, int idSucursal) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT 1 FROM inventario WHERE id_producto = ? AND id_sucursal = ? LIMIT 1")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Baja en la sucursal: quita el inventario del producto solo en esa sucursal. */
    public void quitarProductoDeSucursal(int idProducto, int idSucursal) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM inventario WHERE id_producto = ? AND id_sucursal = ?")) {
            ps.setInt(1, idProducto);
            ps.setInt(2, idSucursal);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------ productos

    /** Productos presentes en la sucursal con sus existencias ahí. */
    public List<Map<String, Object>> productosDeSucursal(int idSucursal) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.imagen, p.id_categoria, c.nombre, "
                + "SUM(i.existencias), COUNT(i.talla) "
                + "FROM inventario i JOIN productos p ON p.id_producto = i.id_producto "
                + "JOIN categorias c ON c.id_categoria = p.id_categoria "
                + "WHERE i.id_sucursal = ? GROUP BY p.id_producto ORDER BY p.nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getInt(1));
                    m.put("nombre", rs.getString(2));
                    m.put("descripcion", rs.getString(3));
                    m.put("precio", rs.getDouble(4));
                    m.put("imagen", rs.getString(5));
                    m.put("idCategoria", rs.getInt(6));
                    m.put("categoria", rs.getString(7));
                    m.put("existencias", rs.getInt(8));
                    m.put("tallas", rs.getInt(9));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    /** Productos activos del catálogo que la sucursal aún no ofrece. */
    public List<Map<String, Object>> productosFueraDeSucursal(int idSucursal) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.id_producto, p.nombre FROM productos p WHERE p.activo = 1 AND NOT EXISTS "
                + "(SELECT 1 FROM inventario i WHERE i.id_producto = p.id_producto AND i.id_sucursal = ?) ORDER BY p.nombre";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", rs.getInt(1));
                    m.put("nombre", rs.getString(2));
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    public int crearProducto(String nombre, String descripcion, double precio, String imagen, int idCategoria)
            throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO productos (nombre, descripcion, precio, imagen, id_categoria, activo) VALUES (?, ?, ?, ?, ?, 1)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setString(4, imagen);
            ps.setInt(5, idCategoria);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void actualizarProducto(int id, String nombre, String descripcion, double precio, String imagen, int idCategoria)
            throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE productos SET nombre = ?, descripcion = ?, precio = ?, imagen = ?, id_categoria = ? WHERE id_producto = ?")) {
            ps.setString(1, nombre);
            ps.setString(2, descripcion);
            ps.setDouble(3, precio);
            ps.setString(4, imagen);
            ps.setInt(5, idCategoria);
            ps.setInt(6, id);
            ps.executeUpdate();
        }
    }

    // ------------------------------------------------------------ pedidos

    /** Pedidos de una sucursal (opcionalmente por estado) con cliente y renglones. */
    public List<Map<String, Object>> pedidosDeSucursal(int idSucursal, String estado, int limite) throws SQLException {
        List<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.fecha, p.total, p.estado, CONCAT(u.nombre, ' ', u.apellidos), u.correo, u.telefono "
                + "FROM pedidos p JOIN usuarios u ON u.id_usuario = p.id_usuario WHERE p.id_sucursal = ? "
                + (estado != null ? "AND p.estado = ? " : "") + "ORDER BY p.id_pedido DESC"
                + (limite > 0 ? " LIMIT " + limite : "");
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSucursal);
            if (estado != null) {
                ps.setString(2, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("idPedido", rs.getInt(1));
                    m.put("fecha", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp(2)));
                    m.put("total", rs.getDouble(3));
                    m.put("estado", rs.getString(4));
                    m.put("cliente", rs.getString(5));
                    m.put("correo", rs.getString(6));
                    m.put("telefono", rs.getString(7));
                    m.put("detalles", new ArrayList<Map<String, Object>>());
                    lista.add(m);
                }
            }
            if (lista.isEmpty()) {
                return lista;
            }
            // Renglones de los pedidos listados
            Map<Integer, Map<String, Object>> porId = new LinkedHashMap<>();
            StringBuilder ids = new StringBuilder();
            for (Map<String, Object> m : lista) {
                porId.put((Integer) m.get("idPedido"), m);
                ids.append(ids.length() == 0 ? "" : ",").append(m.get("idPedido"));
            }
            try (PreparedStatement ps2 = con.prepareStatement(
                    "SELECT d.id_pedido, pr.nombre, d.talla, d.cantidad, d.precio_unitario FROM detalle_pedido d "
                    + "JOIN productos pr ON pr.id_producto = d.id_producto WHERE d.id_pedido IN (" + ids + ") ORDER BY pr.nombre");
                 ResultSet rs = ps2.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> d = new LinkedHashMap<>();
                    d.put("nombre", rs.getString(2));
                    d.put("talla", rs.getString(3));
                    d.put("cantidad", rs.getInt(4));
                    d.put("precioUnitario", rs.getDouble(5));
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> det = (List<Map<String, Object>>) porId.get(rs.getInt(1)).get("detalles");
                    det.add(d);
                }
            }
        }
        return lista;
    }

    /**
     * Cambia el estado de un pedido de la sucursal en una transacción.
     * Al cancelar regresan las existencias; al reactivar un cancelado se vuelven a descontar.
     *
     * @return mensaje de error, o null si se cambió
     */
    public String cambiarEstado(int idPedido, int idSucursal, String nuevo) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                int sucursalPedido;
                String actual;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_sucursal, estado FROM pedidos WHERE id_pedido = ? FOR UPDATE")) {
                    ps.setInt(1, idPedido);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return "El pedido no existe.";
                        }
                        sucursalPedido = rs.getInt(1);
                        actual = rs.getString(2);
                    }
                }
                if (sucursalPedido != idSucursal) {
                    con.rollback();
                    return "Ese pedido es de otra sucursal.";
                }
                if ("entregado".equals(actual) && !"entregado".equals(nuevo)) {
                    con.rollback();
                    return "Un pedido entregado ya no cambia de estado.";
                }
                String ajuste = null;
                if ("cancelado".equals(nuevo) && !"cancelado".equals(actual)) {
                    ajuste = "i.existencias + d.cantidad";
                } else if ("cancelado".equals(actual) && !"cancelado".equals(nuevo)) {
                    ajuste = "GREATEST(0, i.existencias - d.cantidad)";
                }
                if (ajuste != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE inventario i JOIN detalle_pedido d ON d.id_producto = i.id_producto AND d.talla = i.talla "
                            + "SET i.existencias = " + ajuste + " WHERE d.id_pedido = ? AND i.id_sucursal = ?")) {
                        ps.setInt(1, idPedido);
                        ps.setInt(2, idSucursal);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("UPDATE pedidos SET estado = ? WHERE id_pedido = ?")) {
                    ps.setString(1, nuevo);
                    ps.setInt(2, idPedido);
                    ps.executeUpdate();
                }
                con.commit();
                return null;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
