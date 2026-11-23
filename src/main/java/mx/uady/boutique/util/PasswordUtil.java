package mx.uady.boutique.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Cifrado y verificación de contraseñas con BCrypt.
 * <p>
 * Es compatible con los hashes generados por {@code password_hash()} de PHP en la
 * Beta 1.0 (prefijo {@code $2y$}), de modo que las cuentas ya registradas siguen
 * funcionando.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Genera el hash BCrypt de una contraseña.
     *
     * @param password contraseña en texto plano
     * @return hash listo para guardarse en la base de datos
     */
    public static String cifrar(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * Verifica una contraseña contra un hash BCrypt (Java o PHP).
     *
     * @param password contraseña en texto plano
     * @param hash     hash almacenado
     * @return {@code true} si coinciden
     */
    public static boolean verificar(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        // jBCrypt solo reconoce $2a$; PHP genera $2y$ (mismo algoritmo).
        String hashNormalizado = hash.startsWith("$2y$") ? "$2a$" + hash.substring(4) : hash;
        try {
            return BCrypt.checkpw(password, hashNormalizado);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
