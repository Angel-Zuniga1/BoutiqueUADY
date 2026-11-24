package mx.uady.boutique.controller;

import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mx.uady.boutique.dao.AdminDAO;
import mx.uady.boutique.dao.AdministradorDAO;
import mx.uady.boutique.dao.SucursalDAO;
import mx.uady.boutique.model.ResumenVentas;
import mx.uady.boutique.model.Sucursal;
import mx.uady.boutique.util.JsonUtil;

/**
 * Solo principal.
 * GET  /api/admin/sucursales            : lista con resumen por sucursal
 * GET  /api/admin/sucursales?id=N       : detalle de consulta (inventario, ventas, más vendidos, pedidos)
 * POST /api/admin/sucursales {accion: crear|activar|desactivar, nombre|id}
 */
@WebServlet("/api/admin/sucursales")
public class AdminSucursalServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private final AdminDAO adminDAO = new AdminDAO();
    private final AdministradorDAO administradorDAO = new AdministradorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirPrincipal(request, response)) {
            return;
        }
        try {
            String id = request.getParameter("id");
            if (id != null && !id.isEmpty()) {
                detalle(Integer.parseInt(id), response);
                return;
            }
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Sucursal s : sucursalDAO.listarTodas()) {
                ResumenVentas r = adminDAO.resumenVentas(s.getId());
                Map<String, Object> m = mapa();
                m.put("id", s.getId());
                m.put("nombre", s.getNombre());
                m.put("activa", s.isActiva());
                m.put("admins", administradorDAO.nombresDeSucursal(s.getId()));
                m.put("productos", adminDAO.inventarioDe(s.getId()).size());
                m.put("pedidos", r.getPedidos());
                m.put("pendientes", r.pendientes());
                m.put("ventas", r.getVentas());
                lista.add(m);
            }
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, lista);
        } catch (NumberFormatException e) {
            error(response, 400, "Sucursal inválida.");
        } catch (SQLException e) {
            log("Error al leer sucursales", e);
            error(response, 500, "No se pudieron cargar las sucursales.");
        }
    }

    private void detalle(int id, HttpServletResponse response) throws SQLException, IOException {
        String nombre = sucursalDAO.nombreDe(id);
        if (nombre == null) {
            error(response, 404, "Esa sucursal no existe.");
            return;
        }
        List<Map<String, Object>> inventario = adminDAO.inventarioDe(id);
        double valor = 0;
        for (Map<String, Object> p : inventario) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tallas = (List<Map<String, Object>>) p.get("tallas");
            for (Map<String, Object> t : tallas) {
                valor += ((Number) t.get("existencias")).intValue() * ((Number) p.get("precio")).doubleValue();
            }
        }
        Map<String, Object> m = mapa();
        m.put("id", id);
        m.put("nombre", nombre);
        m.put("resumen", adminDAO.resumenVentas(id));
        m.put("valorInventario", valor);
        m.put("inventario", inventario);
        m.put("masVendidos", adminDAO.masVendidos(id));
        m.put("pedidos", adminDAO.pedidosDeSucursal(id, null, 100));
        JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
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
                if (nombre.isEmpty()) {
                    error(response, 400, "Escribe el nombre de la sucursal.");
                } else if (sucursalDAO.existeNombre(nombre)) {
                    error(response, 400, "Ya existe una sucursal con ese nombre.");
                } else {
                    sucursalDAO.crear(nombre);
                    ok(response, "Sucursal \"" + nombre + "\" agregada. Ahora crea su administrador en Administradores.");
                }
            } else if ("activar".equals(accion) || "desactivar".equals(accion)) {
                boolean activa = "activar".equals(accion);
                sucursalDAO.cambiarActiva(entero(json, "id"), activa);
                ok(response, activa ? "Sucursal activada: vuelve a aparecer en la tienda."
                        : "Sucursal desactivada: deja de aparecer en la tienda (su inventario y pedidos se conservan).");
            } else {
                error(response, 400, "Acción no válida.");
            }
        } catch (SQLException e) {
            log("Error al guardar sucursal", e);
            error(response, 500, "No se pudo guardar la sucursal.");
        }
    }
}
