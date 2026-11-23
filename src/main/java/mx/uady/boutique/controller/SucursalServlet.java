package mx.uady.boutique.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import mx.uady.boutique.dao.SucursalDAO;
import mx.uady.boutique.util.JsonUtil;

/** GET /api/sucursales : sucursales activas. */
@WebServlet("/api/sucursales")
public class SucursalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final SucursalDAO dao = new SucursalDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            JsonUtil.enviar(response, 200, dao.listarActivas());
        } catch (SQLException e) {
            log("Error al leer las sucursales", e);
            JsonUtil.enviar(response, 500, JsonUtil.error("No se pudieron cargar las sucursales"));
        }
    }
}
