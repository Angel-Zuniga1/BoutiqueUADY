package mx.uady.boutique.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import mx.uady.boutique.dao.AdministradorDAO;
import mx.uady.boutique.dao.SucursalDAO;
import mx.uady.boutique.util.JsonUtil;
import mx.uady.boutique.util.PasswordUtil;
import mx.uady.boutique.util.Validador;

/**
 * Solo principal: cuentas de administrador de sucursal.
 * GET  : {administradores:[...], sucursales:[activas]}
 * POST : {accion: crear {nombre, correo, password, idSucursal} | activar|desactivar {id} | password {id, password}}
 */
@WebServlet("/api/admin/administradores")
public class AdminAdministradorServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private final AdministradorDAO dao = new AdministradorDAO();
    private final SucursalDAO sucursalDAO = new SucursalDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirPrincipal(request, response)) {
            return;
        }
        try {
            Map<String, Object> m = mapa();
            m.put("administradores", dao.listar());
            m.put("sucursales", sucursalDAO.listarActivas());
            m.put("miId", request.getSession(false).getAttribute(AdminSesion.ID));
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
        } catch (SQLException e) {
            log("Error al leer administradores", e);
            error(response, 500, "No se pudieron cargar los administradores.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirPrincipal(request, response)) {
            return;
        }
        JsonObject json = leerJson(request);
        String accion = texto(json, "accion");
        try {
            if ("crear".equals(accion)) {
                String nombre = texto(json, "nombre");
                String correo = texto(json, "correo");
                String password = texto(json, "password");
                int idSucursal = entero(json, "idSucursal");
                if (nombre.isEmpty() || correo.isEmpty() || password.isEmpty() || idSucursal <= 0) {
                    error(response, 400, "Todos los campos son obligatorios.");
                } else if (!Validador.esCorreoValido(correo)) {
                    error(response, 400, "El correo no es válido.");
                } else if (password.length() < 6) {
                    error(response, 400, "La contraseña debe tener al menos 6 caracteres.");
                } else if (dao.buscarPorCorreo(correo) != null) {
                    error(response, 400, "Ese correo ya tiene una cuenta de administrador.");
                } else {
                    dao.crearDeSucursal(nombre, correo, PasswordUtil.cifrar(password), idSucursal);
                    ok(response, "Cuenta creada. " + correo + " ya puede entrar como administrador de sucursal.");
                }
            } else if ("activar".equals(accion) || "desactivar".equals(accion)) {
                int id = entero(json, "id");
                boolean activo = "activar".equals(accion);
                dao.cambiarActivo(id, activo);
                ok(response, activo ? "Cuenta activada." : "Cuenta desactivada.");
            } else if ("password".equals(accion)) {
                String password = texto(json, "password");
                if (password.length() < 6) {
                    error(response, 400, "La contraseña debe tener al menos 6 caracteres.");
                } else {
                    dao.cambiarPassword(entero(json, "id"), PasswordUtil.cifrar(password));
                    ok(response, "Contraseña actualizada.");
                }
            } else {
                error(response, 400, "Acción no válida.");
            }
        } catch (SQLException e) {
            log("Error al guardar administrador", e);
            error(response, 500, "No se pudo guardar.");
        }
    }
}
