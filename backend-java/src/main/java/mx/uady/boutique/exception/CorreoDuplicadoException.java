package mx.uady.boutique.exception;

/**
 * Se intentó registrar un correo que ya existe.
 * El mensaje se muestra tal cual al usuario.
 */
public class CorreoDuplicadoException extends Exception {

    private static final long serialVersionUID = 1L;

    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
