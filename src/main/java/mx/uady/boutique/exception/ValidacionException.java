package mx.uady.boutique.exception;

/**
 * Datos de entrada incompletos o inválidos.
 * El mensaje se muestra tal cual al usuario.
 */
public class ValidacionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
