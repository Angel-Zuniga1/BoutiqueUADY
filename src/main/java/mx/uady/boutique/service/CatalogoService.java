package mx.uady.boutique.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.uady.boutique.dao.InventarioDAO;
import mx.uady.boutique.dao.ProductoDAO;
import mx.uady.boutique.model.Disponibilidad;
import mx.uady.boutique.model.Producto;

/** Catálogo de productos con su disponibilidad por sucursal y talla. */
public class CatalogoService {

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final InventarioDAO inventarioDAO = new InventarioDAO();

    /**
     * Productos filtrados por sucursal y talla.
     *
     * @param sucursal id de la sucursal; vacío o null = todas las sucursales
     * @param talla    talla pedida; vacío o null = cualquiera
     */
    public List<Producto> filtrar(String sucursal, String talla) throws SQLException {
        List<Producto> productos = productoDAO.listarActivos();
        List<Disponibilidad> inventario = inventarioDAO.listarTodas();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            List<Disponibilidad> disp = new ArrayList<>();
            for (Disponibilidad d : inventario) {
                if (d.getIdProducto() == p.getId()) {
                    if (sucursal == null || sucursal.isEmpty()) {
                        if (talla == null || talla.isEmpty()) {
                            disp.add(d);
                        } else {
                            if (d.getTalla().equals(talla)) {
                                if (d.getExistencias() > 0) {
                                    disp.add(d);
                                }
                            }
                        }
                    } else {
                        if (String.valueOf(d.getIdSucursal()).equals(sucursal)) {
                            if (talla == null || talla.isEmpty()) {
                                if (d.getExistencias() > 0) {
                                    disp.add(d);
                                }
                            } else {
                                if (d.getTalla().equals(talla)) {
                                    if (d.getExistencias() > 0) {
                                        disp.add(d);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (disp.size() > 0) {
                p.setDisponibilidad(disp);
                resultado.add(p);
            }
        }
        return resultado;
    }

    /** Un producto con su disponibilidad completa, o null si no existe. */
    public Producto detalle(int id) throws SQLException {
        Producto p = productoDAO.buscarPorId(id);
        if (p != null) {
            p.setDisponibilidad(inventarioDAO.listarPorProducto(id));
        }
        return p;
    }
}
