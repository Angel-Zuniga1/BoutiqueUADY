package mx.uady.boutique.service;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import mx.uady.boutique.dao.PedidoDAO;
import mx.uady.boutique.model.DetallePedido;
import mx.uady.boutique.model.Pedido;

/** Genera el comprobante de compra en texto plano (.txt). */
public class ComprobanteService {

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    /** Comprobante de un pedido del usuario, o null si el pedido no existe o no es suyo. */
    public String generarDePedido(int idPedido, int idUsuario) throws SQLException {
        Pedido p = pedidoDAO.buscarDeUsuario(idPedido, idUsuario);
        if (p == null) {
            return null;
        }
        return generarTexto(p.getIdPedido(), p.getCliente(), p.getSucursal(), p.getFecha(),
                p.getTotal(), p.getEstado(), p.getDetalles());
    }

    public String generarTexto(int idPedido, String cliente, String sucursal, Date fecha,
                               double total, String estado, List<DetallePedido> detalles) {
        String f = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(fecha);
        String t = "";
        t = t + "==========================================\n";
        t = t + "      BOUTIQUE UADY EN LINEA\n";
        t = t + "      Comprobante de compra\n";
        t = t + "==========================================\n";
        t = t + "Pedido No.: " + idPedido + "\n";
        t = t + "Cliente:    " + cliente + "\n";
        t = t + "Sucursal:   " + sucursal + "\n";
        t = t + "Fecha:      " + f + "\n";
        t = t + "Estado:     " + estado + "\n";
        t = t + "------------------------------------------\n";
        for (DetallePedido d : detalles) {
            t = t + d.getNombre();
            if (d.getTalla() != null && d.getTalla().length() > 0) {
                t = t + " (talla " + d.getTalla() + ")";
            }
            t = t + "\n";
            t = t + "   " + d.getCantidad() + " x $" + String.format(java.util.Locale.US, "%.2f", d.getPrecioUnitario())
                    + " = $" + String.format(java.util.Locale.US, "%.2f", d.getCantidad() * d.getPrecioUnitario()) + "\n";
        }
        t = t + "------------------------------------------\n";
        t = t + "TOTAL: $" + String.format(java.util.Locale.US, "%.2f", total) + " MXN\n";
        t = t + "==========================================\n";
        t = t + "El pago y la entrega se realizan en la sucursal " + sucursal + ".\n";
        t = t + "Gracias por tu compra.\n";
        return t;
    }
}
