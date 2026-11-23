package mx.uady.boutique.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utilería para responder en JSON desde los servlets.
 */
public final class JsonUtil {

    private static final Gson GSON = new Gson();

    private JsonUtil() {
    }

    /** Convierte un objeto a texto JSON. */
    public static String aJson(Object objeto) {
        return GSON.toJson(objeto);
    }

    /** Escribe un objeto como JSON en la respuesta con el código HTTP indicado. */
    public static void enviar(HttpServletResponse response, int estado, Object objeto) throws IOException {
        response.setStatus(estado);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        PrintWriter out = response.getWriter();
        out.print(GSON.toJson(objeto));
        out.flush();
    }

    /** Respuesta estándar: {@code {"ok": ..., "mensaje": "..."}}. */
    public static Map<String, Object> resultado(boolean ok, String mensaje) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("ok", ok);
        mapa.put("mensaje", mensaje);
        return mapa;
    }

    /** Respuesta de error de la API del carrito: {@code {"error": "..."}}. */
    public static Map<String, Object> error(String mensaje) {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("error", mensaje);
        return mapa;
    }
}
