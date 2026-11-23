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
import mx.uady.boutique.controller.SesionUtil;
import mx.uady.boutique.util.JsonUtil;

/** Deja pasar a las rutas privadas (carrito, pedidos y comprobantes) solo a usuarios con sesión. */
@WebFilter(urlPatterns = {"/api/carrito", "/api/pedidos", "/api/comprobante"})
public class AutenticacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (!SesionUtil.haySesion(request)) {
            JsonUtil.enviar(response, HttpServletResponse.SC_UNAUTHORIZED, JsonUtil.error("Sin sesion"));
            return;
        }
        chain.doFilter(req, res);
    }
}
