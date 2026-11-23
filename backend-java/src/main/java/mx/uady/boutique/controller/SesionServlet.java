package mx.uady.boutique.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.uady.boutique.util.JsonUtil;

/**
 * GET /api/sesion: informa si hay sesión iniciada y el nombre del usuario; además mantiene
 * en orden la cookie del saludo (la crea si falta y la borra si la sesión ya no existe).
 * Sustituye las condiciones {@code isset($_SESSION['usuario_id'])} que PHP evaluaba
 * al generar index.php, login.php y registro.php.
 */
@WebServlet("/api/sesion")
public class SesionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> datos = new LinkedHashMap<>();
        HttpSession sesion = request.getSession(false);
        boolean activa = sesion != null && sesion.getAttribute(SesionUtil.USUARIO_ID) != null;
        datos.put("autenticado", activa);
        if (activa) {
            Object nombre = sesion.getAttribute(SesionUtil.USUARIO_NOMBRE);
            datos.put("nombre", nombre);
            // Sesión activa pero sin cookie del saludo (sesión anterior): se vuelve a crear
            if (!SesionUtil.hayCookieNombre(request)) {
                SesionUtil.crearCookieNombre(response, String.valueOf(nombre));
            }
        } else if (SesionUtil.hayCookieNombre(request)) {
            // La sesión expiró pero la cookie sigue: se borra para no mostrar un saludo falso
            SesionUtil.borrarCookieNombre(response);
        }
        JsonUtil.enviar(response, HttpServletResponse.SC_OK, datos);
    }
}
