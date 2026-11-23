package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.DetallePedido;
import mx.uady.boutique.model.Pedido;

/** Consultas de pedidos ya confirmados (la creación está en PedidoService). */
public class PedidoDAO {

    /** Pedido con sus renglones; solo si pertenece al usuario indicado, si no devuelve null. */
    public Pedido buscarDeUsuario(int idPedido, int idUsuario) throws SQLException {
        String sql = "SELECT p.id_pedido, p.id_usuario, CONCAT(u.nombre, ' ', u.apellidos), p.id_sucursal, "
                + "s.nombre, p.fecha, p.total, p.estado "
                + "FROM pedidos p JOIN usuarios u ON u.id_usuario = p.id_usuario "
                + "JOIN sucursales s ON s.id_sucursal = p.id_sucursal "
                + "WHERE p.id_pedido = ? AND p.id_usuario = ?";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Pedido pedido = new Pedido();
                pedido.setIdPedido(rs.getInt(1));
                pedido.setIdUsuario(rs.getInt(2));
                pedido.setCliente(rs.getString(3));
                pedido.setIdSucursal(rs.getInt(4));
                pedido.setSucursal(rs.getString(5));
                pedido.setFecha(rs.getTimestamp(6));
                pedido.setTotal(rs.getDouble(7));
                pedido.setEstado(rs.getString(8));
                pedido.setDetalles(detallesDe(con, idPedido));
                return pedido;
            }
        }
    }

    /** Pedidos del usuario, el más reciente primero (sin renglones). */
    public List<Pedido> listarDeUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT p.id_pedido, p.id_sucursal, s.nombre, p.fecha, p.total, p.estado "
                + "FROM pedidos p JOIN sucursales s ON s.id_sucursal = p.id_sucursal "
                + "WHERE p.id_usuario = ? ORDER BY p.id_pedido DESC";
        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido p = new Pedido();
                    p.setIdPedido(rs.getInt(1));
                    p.setIdUsuario(idUsuario);
                    p.setIdSucursal(rs.getInt(2));
                    p.setSucursal(rs.getString(3));
                    p.setFecha(rs.getTimestamp(4));
                    p.setTotal(rs.getDouble(5));
                    p.setEstado(rs.getString(6));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    private List<DetallePedido> detallesDe(Connection con, int idPedido) throws SQLException {
        String sql = "SELECT d.id_producto, pr.nombre, d.talla, d.cantidad, d.precio_unitario "
                + "FROM detalle_pedido d JOIN productos pr ON pr.id_producto = d.id_producto "
                + "WHERE d.id_pedido = ? ORDER BY pr.nombre, d.talla";
        List<DetallePedido> lista = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetallePedido d = new DetallePedido();
                    d.setIdProducto(rs.getInt(1));
                    d.setNombre(rs.getString(2));
                    d.setTalla(rs.getString(3));
                    d.setCantidad(rs.getInt(4));
                    d.setPrecioUnitario(rs.getDouble(5));
                    lista.add(d);
                }
            }
        }
        return lista;
    }
}
