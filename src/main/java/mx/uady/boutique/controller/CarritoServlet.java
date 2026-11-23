package mx.uady.boutique.controller;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import mx.uady.boutique.service.CarritoService;
import mx.uady.boutique.util.JsonUtil;

/**
 * API del carrito por usuario (antes carrito_api.php).
 * <ul>
 *   <li>GET  /api/carrito: devuelve en JSON el carrito del usuario con sesión.</li>
 *   <li>POST /api/carrito: reemplaza el carrito del usuario con el JSON recibido.</li>
 * </ul>
 * Los precios llegan desde el navegador (igual que en la versión 1.5); al confirmar pedidos
 * reales habrá que volver a leerlos desde la tabla de productos.
 */
@WebServlet("/api/carrito")
public class CarritoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final CarritoService servicio = new CarritoService();
    private final Gson gson = new Gson();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // Solo usuarios con sesión iniciada
        if (!SesionUtil.haySesion(request)) {
            JsonUtil.enviar(response, HttpServletResponse.SC_UNAUTHORIZED, JsonUtil.error("Sin sesion"));
            return;
        }
        int idUsuario = ((Number) request.getSession(false).getAttribute(SesionUtil.USUARIO_ID)).intValue();

        String metodo = request.getMethod();
        if ("GET".equals(metodo)) {
            leer(response, idUsuario);
        } else if ("POST".equals(metodo)) {
            guardar(request, response, idUsuario);
        } else {
            JsonUtil.enviar(response, 405, JsonUtil.error("Metodo no permitido"));
        }
    }

    private void leer(HttpServletResponse response, int idUsuario) throws IOException {
        try {
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, servicio.obtener(idUsuario));
        } catch (SQLException e) {
            log("Error al leer el carrito", e);
            JsonUtil.enviar(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonUtil.error("No se pudo cargar el carrito"));
        }
    }

    private void guardar(HttpServletRequest request, HttpServletResponse response, int idUsuario)
            throws IOException {
        List<Object> datos;
        try {
            datos = gson.fromJson(request.getReader(), new TypeToken<List<Object>>() { }.getType());
        } catch (JsonParseException e) {
            datos = null;
        }
        if (datos == null) {
            JsonUtil.enviar(response, HttpServletResponse.SC_BAD_REQUEST, JsonUtil.error("Datos invalidos"));
            return;
        }

        try {
            servicio.guardar(idUsuario, datos);
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, java.util.Map.of("ok", true));
        } catch (SQLException e) {
            log("Error al guardar el carrito", e);
            JsonUtil.enviar(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonUtil.error("No se pudo guardar el carrito"));
        }
    }
}
