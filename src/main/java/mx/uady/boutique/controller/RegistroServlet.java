package mx.uady.boutique.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import mx.uady.boutique.exception.CorreoDuplicadoException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.service.AutenticacionService;
import mx.uady.boutique.util.JsonUtil;

/**
 * POST /api/registro: crea una cuenta nueva (antes registro.php).
 * Responde JSON con {@code ok} y {@code mensaje}.
 */
@WebServlet("/api/registro")
public class RegistroServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AutenticacionService servicio = new AutenticacionService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");

        // Si el usuario ya inició sesión, se manda al catálogo.
        if (SesionUtil.haySesion(request)) {
            Map<String, Object> r = JsonUtil.resultado(true, "");
            r.put("redirigir", "catalogo.html");
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, r);
            return;
        }

        try {
            servicio.registrar(
                    request.getParameter("nombre"),
                    request.getParameter("apellidos"),
                    request.getParameter("correo"),
                    request.getParameter("telefono"),
                    request.getParameter("password"),
                    request.getParameter("password2"));
            JsonUtil.enviar(response, HttpServletResponse.SC_OK,
                    JsonUtil.resultado(true, "¡Cuenta creada con éxito!"));
        } catch (ValidacionException | CorreoDuplicadoException e) {
            JsonUtil.enviar(response, HttpServletResponse.SC_OK,
                    JsonUtil.resultado(false, e.getMessage()));
        } catch (SQLException e) {
            log("Error al registrar usuario", e);
            JsonUtil.enviar(response, HttpServletResponse.SC_OK,
                    JsonUtil.resultado(false, AutenticacionService.MSG_ERROR_REGISTRO));
        }
    }
}
