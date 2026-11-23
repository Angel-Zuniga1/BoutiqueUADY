package mx.uady.boutique.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.uady.boutique.dao.PedidoDAO;
import mx.uady.boutique.exception.ExistenciasInsuficientesException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.model.Pedido;
import mx.uady.boutique.service.PedidoService;
import mx.uady.boutique.util.JsonUtil;

/**
 * Pedidos del usuario con sesión (la ruta está protegida por AutenticacionFilter).
 * <ul>
 *   <li>POST /api/pedidos  {"idSucursal": 1} : confirma el carrito como pedido.</li>
 *   <li>GET  /api/pedidos : historial de pedidos del usuario.</li>
 * </ul>
 */
@WebServlet("/api/pedidos")
public class PedidoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final PedidoService servicio = new PedidoService();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int idUsuario = ((Number) request.getSession(false).getAttribute(SesionUtil.USUARIO_ID)).intValue();
        int idSucursal = 0;
        try {
            JsonObject json = gson.fromJson(request.getReader(), JsonObject.class);
            if (json != null && json.has("idSucursal")) {
                idSucursal = json.get("idSucursal").getAsInt();
            }
        } catch (JsonParseException | NumberFormatException | IllegalStateException | UnsupportedOperationException e) {
            JsonUtil.enviar(response, 400, JsonUtil.error("Datos invalidos"));
            return;
        }
        try {
            Pedido pedido = servicio.confirmarPedido(idUsuario, idSucursal);
            Map<String, Object> respuesta = new LinkedHashMap<>();
            respuesta.put("ok", true);
            respuesta.put("idPedido", pedido.getIdPedido());
            respuesta.put("total", pedido.getTotal());
            respuesta.put("sucursal", pedido.getSucursal());
            JsonUtil.enviar(response, 200, respuesta);
        } catch (ValidacionException e) {
            JsonUtil.enviar(response, 400, JsonUtil.error(e.getMessage()));
        } catch (ExistenciasInsuficientesException e) {
            JsonUtil.enviar(response, 409, JsonUtil.error(e.getMessage()));
        } catch (SQLException e) {
            log("Error al confirmar el pedido", e);
            JsonUtil.enviar(response, 500, JsonUtil.error("No se pudo confirmar el pedido"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idUsuario = ((Number) request.getSession(false).getAttribute(SesionUtil.USUARIO_ID)).intValue();
        try {
            JsonUtil.enviar(response, 200, pedidoDAO.listarDeUsuario(idUsuario));
        } catch (SQLException e) {
            log("Error al leer los pedidos", e);
            JsonUtil.enviar(response, 500, JsonUtil.error("No se pudieron cargar los pedidos"));
        }
    }
}
