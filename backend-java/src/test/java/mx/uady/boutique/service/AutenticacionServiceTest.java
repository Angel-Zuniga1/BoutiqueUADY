package mx.uady.boutique.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.dao.UsuarioDAO;
import mx.uady.boutique.exception.CorreoDuplicadoException;
import mx.uady.boutique.exception.CredencialesInvalidasException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AutenticacionServiceTest {

    /** DAO en memoria para probar el servicio sin base de datos. */
    private static class UsuarioDAOEnMemoria extends UsuarioDAO {
        final List<Usuario> usuarios = new ArrayList<>();

        @Override
        public boolean existeCorreo(String correo) {
            return usuarios.stream().anyMatch(u -> u.getCorreo().equals(correo));
        }

        @Override
        public boolean insertar(Usuario usuario) {
            usuario.setIdUsuario(usuarios.size() + 1);
            usuarios.add(usuario);
            return true;
        }

        @Override
        public Usuario buscarPorCorreo(String correo) {
            return usuarios.stream().filter(u -> u.getCorreo().equals(correo)).findFirst().orElse(null);
        }
    }

    private UsuarioDAOEnMemoria dao;
    private AutenticacionService servicio;

    @BeforeEach
    void preparar() {
        dao = new UsuarioDAOEnMemoria();
        servicio = new AutenticacionService(dao);
    }

    @Test
    void registraUsuarioConContrasenaCifradaYDatosLimpios() throws Exception {
        servicio.registrar("  Ana ", "Pérez", " ana@uady.mx ", "", "clave1234", "clave1234");

        Usuario guardado = dao.usuarios.get(0);
        assertEquals("Ana", guardado.getNombre());
        assertEquals("ana@uady.mx", guardado.getCorreo());
        assertNotEquals("clave1234", guardado.getPasswordHash());
    }

    @Test
    void correoDuplicadoLanzaExcepcion() throws Exception {
        servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "clave1234");

        CorreoDuplicadoException e = assertThrows(CorreoDuplicadoException.class,
                () -> servicio.registrar("Luis", "Díaz", "ana@uady.mx", "", "otra1234", "otra1234"));
        assertEquals(AutenticacionService.MSG_CORREO_DUPLICADO, e.getMessage());
    }

    @Test
    void registroConContrasenasDistintasLanzaValidacion() {
        assertThrows(ValidacionException.class,
                () -> servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "distinta"));
    }

    @Test
    void inicioDeSesionCorrectoDevuelveUsuario() throws Exception {
        servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "clave1234");

        Usuario u = servicio.iniciarSesion("ana@uady.mx", "clave1234");
        assertEquals("Ana", u.getNombre());
        assertTrue(u.getIdUsuario() > 0);
    }

    @Test
    void contrasenaIncorrectaLanzaCredencialesInvalidas() throws Exception {
        servicio.registrar("Ana", "Pérez", "ana@uady.mx", "", "clave1234", "clave1234");

        CredencialesInvalidasException e = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("ana@uady.mx", "mala"));
        assertEquals(AutenticacionService.MSG_PASSWORD_INCORRECTA, e.getMessage());
    }

    @Test
    void correoInexistenteLanzaCredencialesInvalidas() {
        CredencialesInvalidasException e = assertThrows(CredencialesInvalidasException.class,
                () -> servicio.iniciarSesion("nadie@uady.mx", "clave1234"));
        assertEquals(AutenticacionService.MSG_SIN_CUENTA, e.getMessage());
    }

    @Test
    void loginConCamposVaciosLanzaValidacion() {
        assertThrows(ValidacionException.class, () -> servicio.iniciarSesion("", ""));
    }
}
