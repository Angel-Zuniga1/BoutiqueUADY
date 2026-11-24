package mx.uady.boutique.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.uady.boutique.model.Administrador;
import mx.uady.boutique.util.JsonUtil;

/** Utilidades compartidas por los servlets de /api/admin/*. */
public abstract class AdminServletBase extends HttpServlet {

    private static final long serialVersionUID = 1L;
    protected static final Gson GSON = new Gson();

    /** Lee el cuerpo JSON de la petición (objeto vacío si no hay o no es válido). */
    protected JsonObject leerJson(HttpServletRequest request) throws IOException {
        request.setCharacterEncoding("UTF-8");
        try {
            JsonObject o = GSON.fromJson(request.getReader(), JsonObject.class);
            return o == null ? new JsonObject() : o;
        } catch (JsonParseException e) {
            return new JsonObject();
        }
    }

    protected static String texto(JsonObject o, String clave) {
        return o.has(clave) && !o.get(clave).isJsonNull() ? o.get(clave).getAsString().trim() : "";
    }

    protected static int entero(JsonObject o, String clave) {
        try {
            return o.has(clave) && !o.get(clave).isJsonNull() ? o.get(clave).getAsInt() : 0;
        } catch (NumberFormatException | UnsupportedOperationException e) {
            return 0;
        }
    }

    /** Respuesta {ok:true, mensaje} para avisos con alert() en el panel. */
    protected void ok(HttpServletResponse response, String mensaje) throws IOException {
        JsonUtil.enviar(response, HttpServletResponse.SC_OK, JsonUtil.resultado(true, mensaje));
    }

    protected void error(HttpServletResponse response, int estado, String mensaje) throws IOException {
        JsonUtil.enviar(response, estado, JsonUtil.error(mensaje));
    }

    /** true si el rol de la sesión es el requerido; si no, responde 403. */
    protected boolean exigirRol(HttpServletRequest request, HttpServletResponse response, String rol) throws IOException {
        if (rol.equals(AdminSesion.rol(request))) {
            return true;
        }
        error(response, HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para esa sección.");
        return false;
    }

    protected boolean exigirPrincipal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return exigirRol(request, response, Administrador.ROL_PRINCIPAL);
    }

    protected boolean exigirSucursal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return exigirRol(request, response, Administrador.ROL_SUCURSAL);
    }

    protected static Map<String, Object> mapa() {
        return new LinkedHashMap<>();
    }
}
