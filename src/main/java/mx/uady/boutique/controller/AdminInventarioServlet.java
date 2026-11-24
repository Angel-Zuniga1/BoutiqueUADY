package mx.uady.boutique.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import mx.uady.boutique.dao.AdminDAO;
import mx.uady.boutique.util.JsonUtil;

/**
 * Solo administrador de sucursal (RF-10): existencias por talla DE SU SUCURSAL.
 * GET  : {sucursal, productos:[{id, nombre, activo, tallas:[{talla, existencias}]}]}
 * POST : {accion: guardar, existencias:[{idProducto, talla, cantidad}]} | {accion: agregar, idProducto, talla, cantidad}
 */
@WebServlet("/api/admin/inventario")
public class AdminInventarioServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private static final List<String> TALLAS_VALIDAS = Arrays.asList("", "XS", "S", "M", "L", "XL", "2XL");
    private final AdminDAO dao = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirSucursal(request, response)) {
            return;
        }
        try {
            Map<String, Object> m = mapa();
            m.put("sucursal", AdminSesion.sucursalNombre(request));
            m.put("productos", dao.inventarioDe(AdminSesion.sucursal(request)));
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
        } catch (SQLException e) {
            log("Error al leer inventario", e);
            error(response, 500, "No se pudo cargar el inventario.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirSucursal(request, response)) {
            return;
        }
        int idSuc = AdminSesion.sucursal(request);
        JsonObject json = leerJson(request);
        String accion = texto(json, "accion");
        try {
            if ("guardar".equals(accion)) {
                List<int[]> filas = new ArrayList<>();
                List<String> tallas = new ArrayList<>();
                JsonArray arr = json.has("existencias") && json.get("existencias").isJsonArray()
                        ? json.getAsJsonArray("existencias") : new JsonArray();
                for (JsonElement e : arr) {
                    JsonObject f = e.getAsJsonObject();
                    filas.add(new int[]{entero(f, "idProducto"), entero(f, "cantidad")});
                    tallas.add(texto(f, "talla"));
                }
                dao.guardarExistencias(idSuc, filas, tallas);
                ok(response, "Existencias actualizadas.");
            } else if ("agregar".equals(accion)) {
                int idProducto = entero(json, "idProducto");
                String talla = texto(json, "talla");
                if (idProducto <= 0 || !TALLAS_VALIDAS.contains(talla)) {
                    error(response, 400, "Elige un producto y una talla válidos.");
                } else if (!dao.productoEnSucursal(idProducto, idSuc)) {
                    error(response, 403, "Ese producto no está en tu sucursal.");
                } else {
                    dao.guardarFila(idProducto, idSuc, talla, entero(json, "cantidad"));
                    ok(response, "Talla agregada al inventario de tu sucursal.");
                }
            } else {
                error(response, 400, "Acción no válida.");
            }
        } catch (SQLException e) {
            log("Error al guardar inventario", e);
            error(response, 500, "No se pudo actualizar el inventario.");
        }
    }
}
