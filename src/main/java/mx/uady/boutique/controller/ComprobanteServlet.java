package mx.uady.boutique.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import mx.uady.boutique.service.ComprobanteService;
import mx.uady.boutique.util.JsonUtil;

/** GET /api/comprobante?id=5 : descarga el comprobante .txt de un pedido propio. */
@WebServlet("/api/comprobante")
public class ComprobanteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ComprobanteService servicio = new ComprobanteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int idUsuario = ((Number) request.getSession(false).getAttribute(SesionUtil.USUARIO_ID)).intValue();
        int idPedido;
        try {
            idPedido = Integer.parseInt(request.getParameter("id"));
        } catch (NumberFormatException e) {
            JsonUtil.enviar(response, 400, JsonUtil.error("Pedido invalido"));
            return;
        }
        try {
            String texto = servicio.generarDePedido(idPedido, idUsuario);
            if (texto == null) {
                JsonUtil.enviar(response, 404, JsonUtil.error("Pedido no encontrado"));
                return;
            }
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"comprobante-" + idPedido + ".txt\"");
            response.getOutputStream().write(texto.getBytes(StandardCharsets.UTF_8));
        } catch (SQLException e) {
            log("Error al generar el comprobante", e);
            JsonUtil.enviar(response, 500, JsonUtil.error("No se pudo generar el comprobante"));
        }
    }
}
