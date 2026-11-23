package mx.uady.boutique.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mx.uady.boutique.dao.ConexionBD;
import mx.uady.boutique.exception.ExistenciasInsuficientesException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.model.DetallePedido;
import mx.uady.boutique.model.Pedido;

/** Confirmación de pedidos: convierte el carrito del usuario en un pedido y descuenta el inventario. */
public class PedidoService {

    /**
     * Confirma el pedido del usuario en la sucursal indicada. Todo ocurre en una sola transacción:
     * si falta existencia de cualquier producto no se descuenta nada ni se crea el pedido.
     */
    public Pedido confirmarPedido(int idUsuario, int idSucursal)
            throws SQLException, ValidacionException, ExistenciasInsuficientesException {
        Connection con = ConexionBD.obtenerConexion();
        try {
            con.setAutoCommit(false);

            // 1. validar la sucursal
            String nombreSucursal = null;
            PreparedStatement ps = con.prepareStatement(
                    "SELECT nombre FROM sucursales WHERE id_sucursal = ? AND activa = 1");
            ps.setInt(1, idSucursal);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nombreSucursal = rs.getString(1);
            }
            rs.close();
            ps.close();
            if (nombreSucursal == null) {
                throw new ValidacionException("Selecciona una sucursal válida.");
            }

            // 2. leer el carrito con los precios actuales de la base
            List<DetallePedido> detalles = new ArrayList<>();
            ps = con.prepareStatement("SELECT p.id_producto, p.nombre, d.talla, d.cantidad, p.precio "
                    + "FROM carritos c JOIN detalle_carrito d ON d.id_carrito = c.id_carrito "
                    + "JOIN productos p ON p.id_producto = d.id_producto "
                    + "WHERE c.id_usuario = ? AND p.activo = 1 ORDER BY d.orden");
            ps.setInt(1, idUsuario);
            rs = ps.executeQuery();
            while (rs.next()) {
                DetallePedido d = new DetallePedido();
                d.setIdProducto(rs.getInt(1));
                d.setNombre(rs.getString(2));
                d.setTalla(rs.getString(3));
                d.setCantidad(rs.getInt(4));
                d.setPrecioUnitario(rs.getDouble(5));
                detalles.add(d);
            }
            rs.close();
            ps.close();
            if (detalles.size() == 0) {
                throw new ValidacionException("Tu carrito está vacío.");
            }

            // 3. revisar y descontar existencias (bloquea las filas para evitar ventas dobles)
            double total = 0;
            for (DetallePedido d : detalles) {
                ps = con.prepareStatement("SELECT existencias FROM inventario "
                        + "WHERE id_producto = ? AND id_sucursal = ? AND talla = ? FOR UPDATE");
                ps.setInt(1, d.getIdProducto());
                ps.setInt(2, idSucursal);
                ps.setString(3, d.getTalla());
                rs = ps.executeQuery();
                int existencias = 0;
                if (rs.next()) {
                    existencias = rs.getInt(1);
                }
                rs.close();
                ps.close();
                if (existencias < d.getCantidad()) {
                    String t = d.getTalla().isEmpty() ? "" : " talla " + d.getTalla();
                    throw new ExistenciasInsuficientesException("No hay existencias suficientes de "
                            + d.getNombre() + t + " en la sucursal " + nombreSucursal
                            + " (disponibles: " + existencias + ").");
                }
                ps = con.prepareStatement("UPDATE inventario SET existencias = existencias - ? "
                        + "WHERE id_producto = ? AND id_sucursal = ? AND talla = ?");
                ps.setInt(1, d.getCantidad());
                ps.setInt(2, d.getIdProducto());
                ps.setInt(3, idSucursal);
                ps.setString(4, d.getTalla());
                ps.executeUpdate();
                ps.close();
                total = total + d.getPrecioUnitario() * d.getCantidad();
            }

            // 4. crear el pedido y sus renglones
            ps = con.prepareStatement("INSERT INTO pedidos (id_usuario, id_sucursal, total, estado) "
                    + "VALUES (?, ?, ?, 'pendiente')", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idUsuario);
            ps.setInt(2, idSucursal);
            ps.setDouble(3, total);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            rs.next();
            int idPedido = rs.getInt(1);
            rs.close();
            ps.close();
            for (DetallePedido d : detalles) {
                ps = con.prepareStatement("INSERT INTO detalle_pedido "
                        + "(id_pedido, id_producto, talla, cantidad, precio_unitario) VALUES (?, ?, ?, ?, ?)");
                ps.setInt(1, idPedido);
                ps.setInt(2, d.getIdProducto());
                ps.setString(3, d.getTalla());
                ps.setInt(4, d.getCantidad());
                ps.setDouble(5, d.getPrecioUnitario());
                ps.executeUpdate();
                ps.close();
            }

            // 5. vaciar el carrito
            ps = con.prepareStatement("DELETE d FROM detalle_carrito d "
                    + "JOIN carritos c ON c.id_carrito = d.id_carrito WHERE c.id_usuario = ?");
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
            ps.close();

            con.commit();

            Pedido pedido = new Pedido();
            pedido.setIdPedido(idPedido);
            pedido.setIdUsuario(idUsuario);
            pedido.setIdSucursal(idSucursal);
            pedido.setSucursal(nombreSucursal);
            pedido.setFecha(new Date());
            pedido.setTotal(total);
            pedido.setEstado("pendiente");
            pedido.setDetalles(detalles);
            return pedido;
        } catch (SQLException | ValidacionException | ExistenciasInsuficientesException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.close();
        }
    }
}
