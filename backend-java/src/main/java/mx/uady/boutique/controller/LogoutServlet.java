package mx.uady.boutique.controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * /api/logout: cierra la sesión y redirige al inicio (antes logout.php).
 */
@WebServlet("/api/logout")
public class LogoutServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        cerrarSesion(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        cerrarSesion(request, response);
    }

    private void cerrarSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        // Borrar la cookie del saludo
        SesionUtil.borrarCookieNombre(response);
        // Redirección relativa (/BoutiqueUADY/api/logout -> /BoutiqueUADY/index.html):
        // el navegador la resuelve sobre el puerto 80 de Apache, no sobre el 8080 de Tomcat.
        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", "../index.html");
    }
}
