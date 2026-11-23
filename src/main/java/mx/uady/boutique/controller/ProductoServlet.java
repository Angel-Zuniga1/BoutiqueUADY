package mx.uady.boutique.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import mx.uady.boutique.model.Producto;
import mx.uady.boutique.service.CatalogoService;
import mx.uady.boutique.util.JsonUtil;

/**
 * Catálogo público.
 * <ul>
 *   <li>GET /api/productos?sucursal=1&amp;talla=M : lista filtrada (sin parámetros: todo).</li>
 *   <li>GET /api/productos?id=3 : detalle de un producto con su disponibilidad.</li>
 * </ul>
 */
@WebServlet("/api/productos")
public class ProductoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final CatalogoService servicio = new CatalogoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id != null && !id.isEmpty()) {
                int numero;
                try {
                    numero = Integer.parseInt(id);
                } catch (NumberFormatException e) {
                    JsonUtil.enviar(response, 400, JsonUtil.error("Producto invalido"));
                    return;
                }
                Producto p = servicio.detalle(numero);
                if (p == null) {
                    JsonUtil.enviar(response, 404, JsonUtil.error("Producto no encontrado"));
                } else {
                    JsonUtil.enviar(response, 200, p);
                }
                return;
            }
            JsonUtil.enviar(response, 200,
                    servicio.filtrar(request.getParameter("sucursal"), request.getParameter("talla")));
        } catch (SQLException e) {
            log("Error al leer el catalogo", e);
            JsonUtil.enviar(response, 500, JsonUtil.error("No se pudo cargar el catalogo"));
        }
    }
}
