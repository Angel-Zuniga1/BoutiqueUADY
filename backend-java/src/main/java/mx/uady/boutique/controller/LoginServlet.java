package mx.uady.boutique.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import mx.uady.boutique.exception.CredencialesInvalidasException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.model.Usuario;
import mx.uady.boutique.service.AutenticacionService;
import mx.uady.boutique.util.JsonUtil;

/**
 * POST /api/login: inicia sesión (antes login.php).
 * Si las credenciales son correctas guarda {@code usuario_id} y {@code usuario_nombre}
 * en la sesión, crea la cookie del saludo y pide redirigir al catálogo.
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AutenticacionService servicio = new AutenticacionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        if (SesionUtil.haySesion(request)) {
            // Si falta la cookie de la barra (sesión anterior), se vuelve a crear
            if (!SesionUtil.hayCookieNombre(request)) {
                SesionUtil.crearCookieNombre(response,
                        String.valueOf(request.getSession(false).getAttribute(SesionUtil.USUARIO_NOMBRE)));
            }
            Map<String, Object> r = JsonUtil.resultado(true, "");
            r.put("redirigir", "catalogo.html");
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, r);
            return;
        }

        try {
            Usuario usuario = servicio.iniciarSesion(
                    request.getParameter("correo"),
                    request.getParameter("password"));

            // Nuevo identificador de sesión al autenticarse (evita fijación de sesión).
            HttpSession sesion = request.getSession(true);
            request.changeSessionId();
            sesion.setAttribute(SesionUtil.USUARIO_ID, usuario.getIdUsuario());
            sesion.setAttribute(SesionUtil.USUARIO_NOMBRE, usuario.getNombre());

            // Cookie con el nombre para que las páginas .html muestren el saludo
            SesionUtil.crearCookieNombre(response, usuario.getNombre());

            Map<String, Object> r = JsonUtil.resultado(true, "");
            r.put("redirigir", "catalogo.html");
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, r);
        } catch (ValidacionException | CredencialesInvalidasException e) {
            JsonUtil.enviar(response, HttpServletResponse.SC_OK,
                    JsonUtil.resultado(false, e.getMessage()));
        } catch (SQLException e) {
            log("Error al iniciar sesión", e);
            JsonUtil.enviar(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonUtil.resultado(false, "Error de conexión a la base de datos."));
        }
    }
}
