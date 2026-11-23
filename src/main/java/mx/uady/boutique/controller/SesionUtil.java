package mx.uady.boutique.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Atributos de sesión y cookie del saludo (los mismos nombres que usaba PHP).
 * <p>
 * Las páginas .html no pueden leer la sesión del servidor, así que al iniciar sesión se crea
 * la cookie {@code usuario_nombre} (visible para JavaScript, vive hasta cerrar el navegador).
 * script.js la lee para mostrar «Hola, nombre» y carrito.js para saber si hay sesión.
 */
public final class SesionUtil {

    public static final String USUARIO_ID = "usuario_id";
    public static final String USUARIO_NOMBRE = "usuario_nombre";
    public static final String COOKIE_NOMBRE = "usuario_nombre";

    private SesionUtil() {
    }

    public static boolean haySesion(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null && sesion.getAttribute(USUARIO_ID) != null;
    }

    /** Indica si el navegador ya envió la cookie del saludo. */
    static boolean hayCookieNombre(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie c : cookies) {
            if (COOKIE_NOMBRE.equals(c.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Crea la cookie del saludo. El valor se codifica como {@code rawurlencode} de PHP
     * (espacios como %20) para que {@code decodeURIComponent} lo lea bien.
     */
    static void crearCookieNombre(HttpServletResponse response, String nombre) {
        String valor = URLEncoder.encode(nombre, StandardCharsets.UTF_8).replace("+", "%20");
        Cookie cookie = new Cookie(COOKIE_NOMBRE, valor);
        cookie.setPath("/");
        cookie.setMaxAge(-1);          // cookie de sesión: se borra al cerrar el navegador
        cookie.setHttpOnly(false);     // JavaScript necesita leerla
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    /** Borra la cookie del saludo. */
    static void borrarCookieNombre(HttpServletResponse response) {
        Cookie cookie = new Cookie(COOKIE_NOMBRE, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
