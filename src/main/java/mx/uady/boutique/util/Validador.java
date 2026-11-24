package mx.uady.boutique.util;

import mx.uady.boutique.exception.ValidacionException;

/**
 * Validaciones de los formularios de registro e inicio de sesión.
 * Los mensajes son los mismos que mostraba la Beta 1.0.
 */
public final class Validador {

    public static final String MSG_CAMPOS_OBLIGATORIOS =
            "Por favor, llena todos los campos obligatorios.";
    public static final String MSG_PASSWORDS_DISTINTAS = "Las contraseñas no coinciden.";
    public static final String MSG_LOGIN_VACIO = "Por favor, ingresa tu correo y contraseña.";

    private Validador() {
    }

    /** @return {@code true} si el texto es nulo o está vacío. */
    public static boolean estaVacio(String texto) {
        return texto == null || texto.isEmpty();
    }

    /** Comprobación sencilla de formato de correo (la misma expresión que validacion.js). */
    public static boolean esCorreoValido(String correo) {
        return correo != null && correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    /** Elimina espacios al inicio y al final; un valor nulo se vuelve cadena vacía. */
    public static String limpiar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    /**
     * Valida los datos del formulario de registro.
     *
     * @throws ValidacionException si falta algún campo obligatorio o las contraseñas no coinciden
     */
    public static void validarRegistro(String nombre, String apellidos, String correo,
                                       String password, String password2) throws ValidacionException {
        if (estaVacio(nombre) || estaVacio(apellidos) || estaVacio(correo) || estaVacio(password)) {
            throw new ValidacionException(MSG_CAMPOS_OBLIGATORIOS);
        }
        if (!password.equals(password2)) {
            throw new ValidacionException(MSG_PASSWORDS_DISTINTAS);
        }
    }

    /**
     * Valida los datos del formulario de inicio de sesión.
     *
     * @throws ValidacionException si falta el correo o la contraseña
     */
    public static void validarLogin(String correo, String password) throws ValidacionException {
        if (estaVacio(correo) || estaVacio(password)) {
            throw new ValidacionException(MSG_LOGIN_VACIO);
        }
    }
}
