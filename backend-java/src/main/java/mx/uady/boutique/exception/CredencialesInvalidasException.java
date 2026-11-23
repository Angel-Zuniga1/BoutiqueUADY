package mx.uady.boutique.exception;

/**
 * Correo o contraseña incorrectos al iniciar sesión.
 * El mensaje se muestra tal cual al usuario.
 */
public class CredencialesInvalidasException extends Exception {

    private static final long serialVersionUID = 1L;

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
