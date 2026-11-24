package mx.uady.boutique.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import mx.uady.boutique.exception.CredencialesInvalidasException;
import mx.uady.boutique.model.Administrador;
import mx.uady.boutique.service.AdminAutenticacionService;
import mx.uady.boutique.util.JsonUtil;

/**
 * Sesión del panel.
 * GET    : {llave, autenticado, nombre, rol, idSucursal, sucursal}
 * POST   : paso 2, login de administradores {correo, password} (requiere la llave del paso 1)
 * DELETE : salir del panel
 */
@WebServlet("/api/admin/sesion")
public class AdminSesionServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private final AdminAutenticacionService servicio = new AdminAutenticacionService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> m = mapa();
        m.put("llave", AdminSesion.hayLlave(request));
        m.put("autenticado", AdminSesion.haySesion(request));
        if (AdminSesion.haySesion(request)) {
            m.put("nombre", AdminSesion.nombre(request));
            m.put("rol", AdminSesion.rol(request));
            m.put("idSucursal", AdminSesion.sucursal(request));
            m.put("sucursal", AdminSesion.sucursalNombre(request));
        }
        JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AdminSesion.hayLlave(request) && !AdminSesion.haySesion(request)) {
            error(response, HttpServletResponse.SC_UNAUTHORIZED, "Primero entra con la cuenta de acceso en la tienda.");
            return;
        }
        JsonObject json = leerJson(request);
        try {
            Administrador a = servicio.iniciarSesion(texto(json, "correo"), texto(json, "password"));
            AdminSesion.iniciar(request, a);
            String saludo = a.esPrincipal()
                    ? "Bienvenido, " + a.getNombre() + " (administrador principal)."
                    : "Bienvenido, " + a.getNombre() + ". Estás administrando la sucursal " + a.getSucursal() + ".";
            Map<String, Object> r = JsonUtil.resultado(true, saludo);
            r.put("rol", a.getRol());
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, r);
        } catch (CredencialesInvalidasException e) {
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, JsonUtil.resultado(false, e.getMessage()));
        } catch (SQLException e) {
            log("Error en login de administradores", e);
            JsonUtil.enviar(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    JsonUtil.resultado(false, "Error de conexión a la base de datos."));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        AdminSesion.cerrar(request);
        ok(response, "Sesión de administrador cerrada.");
    }
}
