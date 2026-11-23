package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.Disponibilidad;

public class InventarioDAO {

    /** Existencias de todos los productos en las sucursales activas. */
    public List<Disponibilidad> listarTodas() throws SQLException {
        return consultar(0);
    }

    /** Existencias de un solo producto en las sucursales activas. */
    public List<Disponibilidad> listarPorProducto(int idProducto) throws SQLException {
        return consultar(idProducto);
    }

    private List<Disponibilidad> consultar(int idProducto) throws SQLException {
        String sql = "SELECT i.id_producto, i.id_sucursal, s.nombre, i.talla, i.existencias "
                + "FROM inventario i JOIN sucursales s ON s.id_sucursal = i.id_sucursal "
                + "WHERE s.activa = 1 "
                + (idProducto > 0 ? "AND i.id_producto = ? " : "")
                + "ORDER BY i.id_producto, i.id_sucursal, FIELD(i.talla, '', 'XS', 'S', 'M', 'L', 'XL', '2XL')";
        List<Disponibilidad> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (idProducto > 0) {
                ps.setInt(1, idProducto);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Disponibilidad(rs.getInt(1), rs.getInt(2), rs.getString(3),
                            rs.getString(4), rs.getInt(5)));
                }
            }
        }
        return lista;
    }
}
