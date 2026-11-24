package mx.uady.boutique.service;

import java.sql.SQLException;
import mx.uady.boutique.dao.AdministradorDAO;
import mx.uady.boutique.exception.CredencialesInvalidasException;
import mx.uady.boutique.model.Administrador;
import mx.uady.boutique.util.PasswordUtil;

/**
 * Acceso de administradores en dos pasos:
 * paso 1, la cuenta "llave" en el login de la tienda abre el login de administradores;
 * paso 2, el principal o el de sucursal entran con su propia cuenta.
 */
public class AdminAutenticacionService {

    public static final String MSG_CREDENCIALES = "Correo o contraseña incorrectos.";
    public static final String MSG_DESACTIVADA = "Esta cuenta de administrador está desactivada.";

    private final AdministradorDAO dao;

    public AdminAutenticacionService() {
        this(new AdministradorDAO());
    }

    public AdminAutenticacionService(AdministradorDAO dao) {
        this.dao = dao;
    }

    /** Paso 1: true si correo y contraseña son de la cuenta llave activa. */
    public boolean esLlave(String correo, String password) throws SQLException {
        if (correo == null || password == null) {
            return false;
        }
        Administrador a = dao.buscarPorCorreo(correo.trim());
        return a != null && Administrador.ROL_LLAVE.equals(a.getRol()) && a.isActivo()
                && PasswordUtil.verificar(password, a.getPasswordHash());
    }

    /** Paso 2: devuelve la cuenta principal o de sucursal autenticada. */
    public Administrador iniciarSesion(String correo, String password)
            throws SQLException, CredencialesInvalidasException {
        if (correo == null || password == null || correo.trim().isEmpty() || password.isEmpty()) {
            throw new CredencialesInvalidasException("Ingresa tu correo y contraseña de administrador.");
        }
        Administrador a = dao.buscarPorCorreo(correo.trim());
        if (a == null || Administrador.ROL_LLAVE.equals(a.getRol())
                || !PasswordUtil.verificar(password, a.getPasswordHash())) {
            throw new CredencialesInvalidasException(MSG_CREDENCIALES);
        }
        if (!a.isActivo()) {
            throw new CredencialesInvalidasException(MSG_DESACTIVADA);
        }
        return a;
    }
}
