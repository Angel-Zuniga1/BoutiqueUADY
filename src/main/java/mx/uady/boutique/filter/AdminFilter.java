package mx.uady.boutique.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.uady.boutique.controller.AdminSesion;
import mx.uady.boutique.util.JsonUtil;

/**
 * Protege las APIs del panel (/api/admin/*): requieren sesión de administrador.
 * La única excepción es /api/admin/sesion, que atiende la llave y el login del panel.
 */
@WebFilter(urlPatterns = {"/api/admin/*"})
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (ruta.equals("/api/admin/sesion") || AdminSesion.haySesion(request)) {
            chain.doFilter(req, res);
            return;
        }
        JsonUtil.enviar(response, HttpServletResponse.SC_UNAUTHORIZED, JsonUtil.error("Sin sesion de administrador"));
    }
}
