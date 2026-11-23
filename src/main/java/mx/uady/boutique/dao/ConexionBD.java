package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Conexión JDBC a la base de datos {@code boutique_uady} del MySQL/MariaDB de XAMPP.
 * Mismos parámetros que usaba {@code conexion.php} en la Beta 1.0.
 */
public final class ConexionBD {

    private static final String HOST = "localhost";
    private static final int PUERTO = 3306;
    private static final String NOMBRE_BD = "boutique_uady";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "";

    private static final String URL = "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + NOMBRE_BD
            + "?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
            + "&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private ConexionBD() {
    }

    /**
     * Abre una nueva conexión. Quien la solicita debe cerrarla (try-with-resources).
     *
     * @throws SQLException si no se puede conectar a la base de datos
     */
    public static Connection obtenerConexion() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el controlador JDBC de MySQL.", e);
        }
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
