package mx.uady.boutique.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import mx.uady.boutique.model.Administrador;

/**
 * Sesión del panel de administración. Usa atributos propios (admin_*) que NUNCA se
 * mezclan con la sesión de cliente (usuario_id) ni con la cookie del saludo.
 */
public final class AdminSesion {

    public static final String LLAVE = "llave_admin";
    public static final String ID = "admin_id";
    public static final String NOMBRE = "admin_nombre";
    public static final String ROL = "admin_rol";
    public static final String SUCURSAL = "admin_sucursal";
    public static final String SUCURSAL_NOMBRE = "admin_sucursal_nombre";

    private AdminSesion() {
    }

    public static boolean hayLlave(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s != null && Boolean.TRUE.equals(s.getAttribute(LLAVE));
    }

    public static boolean haySesion(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s != null && s.getAttribute(ID) != null;
    }

    public static boolean esPrincipal(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s != null && Administrador.ROL_PRINCIPAL.equals(s.getAttribute(ROL));
    }

    public static String rol(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        return s == null ? null : (String) s.getAttribute(ROL);
    }

    public static String nombre(HttpServletRequest request) {
        return (String) request.getSession(false).getAttribute(NOMBRE);
    }

    /** Sucursal asignada al administrador de sucursal (0 si es principal). */
    public static int sucursal(HttpServletRequest request) {
        Object v = request.getSession(false).getAttribute(SUCURSAL);
        return v == null ? 0 : ((Number) v).intValue();
    }

    public static String sucursalNombre(HttpServletRequest request) {
        Object v = request.getSession(false).getAttribute(SUCURSAL_NOMBRE);
        return v == null ? "" : (String) v;
    }

    public static void iniciar(HttpServletRequest request, Administrador a) {
        HttpSession s = request.getSession(true);
        request.changeSessionId();
        s.setAttribute(ID, a.getIdAdmin());
        s.setAttribute(NOMBRE, a.getNombre());
        s.setAttribute(ROL, a.getRol());
        s.setAttribute(SUCURSAL, a.getIdSucursal());
        s.setAttribute(SUCURSAL_NOMBRE, a.getSucursal());
        s.removeAttribute(LLAVE);
    }

    public static void cerrar(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        if (s == null) {
            return;
        }
        for (String k : new String[]{LLAVE, ID, NOMBRE, ROL, SUCURSAL, SUCURSAL_NOMBRE}) {
            s.removeAttribute(k);
        }
    }
}
