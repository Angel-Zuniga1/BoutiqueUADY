package mx.uady.boutique.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import mx.uady.boutique.exception.ValidacionException;
import org.junit.jupiter.api.Test;

class ValidadorTest {

    @Test
    void detectaCamposVacios() {
        assertTrue(Validador.estaVacio(null));
        assertTrue(Validador.estaVacio(""));
        assertFalse(Validador.estaVacio("Ana"));
    }

    @Test
    void limpiarQuitaEspaciosYConvierteNuloEnVacio() {
        assertEquals("Ana", Validador.limpiar("  Ana  "));
        assertEquals("", Validador.limpiar(null));
    }

    @Test
    void registroConCampoObligatorioVacioFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarRegistro("", "Pérez", "a@b.com", "12345678", "12345678"));
        assertEquals(Validador.MSG_CAMPOS_OBLIGATORIOS, e.getMessage());
    }

    @Test
    void registroConContrasenasDistintasFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarRegistro("Ana", "Pérez", "a@b.com", "12345678", "87654321"));
        assertEquals(Validador.MSG_PASSWORDS_DISTINTAS, e.getMessage());
    }

    @Test
    void registroValidoNoLanzaExcepcion() throws ValidacionException {
        Validador.validarRegistro("Ana", "Pérez", "a@b.com", "12345678", "12345678");
    }

    @Test
    void loginVacioFalla() {
        ValidacionException e = assertThrows(ValidacionException.class,
                () -> Validador.validarLogin("a@b.com", ""));
        assertEquals(Validador.MSG_LOGIN_VACIO, e.getMessage());
    }
}
