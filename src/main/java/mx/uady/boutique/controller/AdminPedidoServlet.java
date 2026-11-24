package mx.uady.boutique.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import mx.uady.boutique.dao.AdminDAO;
import mx.uady.boutique.model.ResumenVentas;
import mx.uady.boutique.util.JsonUtil;

/**
 * Solo administrador de sucursal (RF-11): pedidos y ventas DE SU SUCURSAL.
 * GET  ?estado= : {sucursal, resumen, pedidos:[...]}
 * POST {idPedido, estado} : cambia el estado (al cancelar regresan las existencias)
 */
@WebServlet("/api/admin/pedidos")
public class AdminPedidoServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private final AdminDAO dao = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirSucursal(request, response)) {
            return;
        }
        int idSuc = AdminSesion.sucursal(request);
        String estado = request.getParameter("estado");
        if (estado != null && !ResumenVentas.esEstadoValido(estado)) {
            estado = null;
        }
        try {
            Map<String, Object> m = mapa();
            m.put("sucursal", AdminSesion.sucursalNombre(request));
            m.put("resumen", dao.resumenVentas(idSuc));
            m.put("pedidos", dao.pedidosDeSucursal(idSuc, estado, 0));
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
        } catch (SQLException e) {
            log("Error al leer pedidos", e);
            error(response, 500, "No se pudieron cargar los pedidos.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirSucursal(request, response)) {
            return;
        }
        JsonObject json = leerJson(request);
        int idPedido = entero(json, "idPedido");
        String estado = texto(json, "estado");
        if (!ResumenVentas.esEstadoValido(estado)) {
            error(response, 400, "Estado no válido.");
            return;
        }
        try {
            String fallo = dao.cambiarEstado(idPedido, AdminSesion.sucursal(request), estado);
            if (fallo != null) {
                error(response, 400, fallo);
            } else {
                ok(response, "Pedido #" + idPedido + " ahora está: " + estado + ".");
            }
        } catch (SQLException e) {
            log("Error al cambiar estado", e);
            error(response, 500, "No se pudo cambiar el estado.");
        }
    }
}
