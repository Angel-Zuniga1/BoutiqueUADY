package mx.uady.boutique.service;

import java.sql.SQLException;
import mx.uady.boutique.dao.UsuarioDAO;
import mx.uady.boutique.exception.CorreoDuplicadoException;
import mx.uady.boutique.exception.CredencialesInvalidasException;
import mx.uady.boutique.exception.ValidacionException;
import mx.uady.boutique.model.Usuario;
import mx.uady.boutique.util.PasswordUtil;
import mx.uady.boutique.util.Validador;

/**
 * Reglas de negocio de registro e inicio de sesión.
 */
public class AutenticacionService {

    public static final String MSG_CORREO_DUPLICADO = "Este correo ya está registrado. Intenta con otro.";
    public static final String MSG_ERROR_REGISTRO = "Error al crear la cuenta. Intenta de nuevo.";
    public static final String MSG_SIN_CUENTA = "Correo o contraseña incorrectos.";
    public static final String MSG_PASSWORD_INCORRECTA = "Correo o contraseña incorrectos.";

    private final UsuarioDAO usuarioDAO;

    public AutenticacionService() {
        this(new UsuarioDAO());
    }

    public AutenticacionService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Registra un nuevo usuario.
     *
     * @throws ValidacionException      campos obligatorios vacíos o contraseñas distintas
     * @throws CorreoDuplicadoException el correo ya existe
     * @throws SQLException             falla de base de datos
     */
    public void registrar(String nombre, String apellidos, String correo, String telefono,
                          String password, String password2)
            throws ValidacionException, CorreoDuplicadoException, SQLException {
        nombre = Validador.limpiar(nombre);
        apellidos = Validador.limpiar(apellidos);
        correo = Validador.limpiar(correo);
        telefono = Validador.limpiar(telefono);

        Validador.validarRegistro(nombre, apellidos, correo, password, password2);

        if (usuarioDAO.existeCorreo(correo)) {
            throw new CorreoDuplicadoException(MSG_CORREO_DUPLICADO);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setCorreo(correo);
        usuario.setTelefono(telefono);
        usuario.setPasswordHash(PasswordUtil.cifrar(password));

        if (!usuarioDAO.insertar(usuario)) {
            throw new SQLException(MSG_ERROR_REGISTRO);
        }
    }

    /**
     * Valida las credenciales y devuelve el usuario autenticado.
     *
     * @throws ValidacionException            correo o contraseña vacíos
     * @throws CredencialesInvalidasException la cuenta no existe o la contraseña es incorrecta
     * @throws SQLException                   falla de base de datos
     */
    public Usuario iniciarSesion(String correo, String password)
            throws ValidacionException, CredencialesInvalidasException, SQLException {
        correo = Validador.limpiar(correo);

        Validador.validarLogin(correo, password);

        Usuario usuario = usuarioDAO.buscarPorCorreo(correo);
        if (usuario == null) {
            throw new CredencialesInvalidasException(MSG_SIN_CUENTA);
        }
        if (!PasswordUtil.verificar(password, usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException(MSG_PASSWORD_INCORRECTA);
        }
        return usuario;
    }
}
