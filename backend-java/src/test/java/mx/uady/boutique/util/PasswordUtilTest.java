package mx.uady.boutique.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {

    @Test
    void cifrarYVerificarContrasenaCorrecta() {
        String hash = PasswordUtil.cifrar("miClave123");
        assertNotEquals("miClave123", hash);
        assertTrue(PasswordUtil.verificar("miClave123", hash));
    }

    @Test
    void rechazaContrasenaIncorrecta() {
        String hash = PasswordUtil.cifrar("miClave123");
        assertFalse(PasswordUtil.verificar("otraClave", hash));
    }

    @Test
    void aceptaHashGeneradoPorPhpConPrefijo2y() {
        String hashJava = PasswordUtil.cifrar("secreto88");
        String comoPhp = "$2y$" + hashJava.substring(4);
        assertTrue(PasswordUtil.verificar("secreto88", comoPhp));
    }

    @Test
    void hashInvalidoONuloNoVerifica() {
        assertFalse(PasswordUtil.verificar("x", "no-es-un-hash"));
        assertFalse(PasswordUtil.verificar(null, "hash"));
        assertFalse(PasswordUtil.verificar("x", null));
    }
}
