package mx.uady.boutique.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.model.Sucursal;

public class SucursalDAO {

    public List<Sucursal> listarActivas() throws SQLException {
        List<Sucursal> lista = new ArrayList<>();
        String sql = "SELECT id_sucursal, nombre FROM sucursales WHERE activa = 1 ORDER BY id_sucursal";
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Sucursal(rs.getInt("id_sucursal"), rs.getString("nombre")));
            }
        }
        return lista;
    }
}
