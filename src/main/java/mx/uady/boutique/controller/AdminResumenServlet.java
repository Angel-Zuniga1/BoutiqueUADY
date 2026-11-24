package mx.uady.boutique.controller;

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

/** GET /api/admin/resumen : datos del inicio del panel según el rol. */
@WebServlet("/api/admin/resumen")
public class AdminResumenServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private final AdminDAO adminDAO = new AdminDAO();
    private final SucursalDAO sucursalDAO = new SucursalDAO();
    private final AdministradorDAO administradorDAO = new AdministradorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Map<String, Object> m = mapa();
            if (AdminSesion.esPrincipal(request)) {
                ResumenVentas global = adminDAO.resumenVentas(0);
                m.put("ventas", global.getVentas());
                m.put("pendientes", global.pendientes());
                m.put("admins", administradorDAO.contarActivosDeSucursal());
                List<Map<String, Object>> porSucursal = new ArrayList<>();
                int activas = 0;
                for (Sucursal s : sucursalDAO.listarTodas()) {
                    if (s.isActiva()) {
                        activas++;
                    }
                    ResumenVentas r = adminDAO.resumenVentas(s.getId());
                    Map<String, Object> fila = mapa();
                    fila.put("id", s.getId());
                    fila.put("nombre", s.getNombre());
                    fila.put("activa", s.isActiva());
                    fila.put("pedidos", r.getPedidos());
                    fila.put("porEstado", r.getPorEstado());
                    fila.put("ventas", r.getVentas());
                    porSucursal.add(fila);
                }
                m.put("sucursalesActivas", activas);
                m.put("porSucursal", porSucursal);
            } else {
                int idSuc = AdminSesion.sucursal(request);
                ResumenVentas r = adminDAO.resumenVentas(idSuc);
                m.put("sucursal", AdminSesion.sucursalNombre(request));
                m.put("ventas", r.getVentas());
                m.put("porEstado", r.getPorEstado());
                m.put("agotados", adminDAO.contarAgotados(idSuc));
                m.put("productos", adminDAO.contarProductosDeSucursal(idSuc));
            }
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
        } catch (SQLException e) {
            log("Error en resumen admin", e);
            error(response, 500, "No se pudo cargar el resumen.");
        }
    }
}
