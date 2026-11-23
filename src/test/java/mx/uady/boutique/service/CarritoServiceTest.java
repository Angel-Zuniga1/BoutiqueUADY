package mx.uady.boutique.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.uady.boutique.dao.CarritoDAO;
import mx.uady.boutique.model.ItemCarrito;
import org.junit.jupiter.api.Test;

class CarritoServiceTest {

    private static Map<String, Object> producto(Object id, Object talla, Object nombre,
                                                Object precio, Object cantidad) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("talla", talla);
        m.put("nombre", nombre);
        m.put("precio", precio);
        m.put("imagen", "fotos boutique/x.jpg");
        m.put("cantidad", cantidad);
        return m;
    }

    @Test
    void aceptaProductoValidoConTalla() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("1", "M", "Playera", 180.0, 2.0)));
        assertEquals(1, r.size());
        assertEquals("M", r.get(0).getTalla());
        assertEquals(2, r.get(0).getCantidad());
        assertEquals(180.0, r.get(0).getPrecio());
    }

    @Test
    void aceptaAccesorioSinTalla() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("8", "", "Llavero", 60.0, 1.0)));
        assertEquals(1, r.size());
        assertEquals("", r.get(0).getTalla());
    }

    @Test
    void descartaTallaInvalida() {
        assertTrue(CarritoService.limpiar(List.of(producto("1", "XXL", "Playera", 180.0, 1.0))).isEmpty());
    }

    @Test
    void descartaCantidadMenorAUnoPrecioNegativoIdONombreVacios() {
        List<Map<String, Object>> datos = List.of(
                producto("1", "M", "Playera", 180.0, 0.0),
                producto("2", "M", "Playera", -5.0, 1.0),
                producto("", "M", "Playera", 180.0, 1.0),
                producto("3", "M", "   ", 180.0, 1.0));
        assertTrue(CarritoService.limpiar(datos).isEmpty());
    }

    @Test
    void limitaCantidadA99() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(producto("1", "M", "Playera", 180.0, 500.0)));
        assertEquals(99, r.get(0).getCantidad());
    }

    @Test
    void mismoProductoYTallaSeSuman() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(
                producto("1", "M", "Playera", 180.0, 60.0),
                producto("1", "M", "Playera", 180.0, 60.0)));
        assertEquals(1, r.size());
        assertEquals(99, r.get(0).getCantidad());
    }

    @Test
    void mismaPrendaConOtraTallaEsOtraLinea() {
        List<ItemCarrito> r = CarritoService.limpiar(List.of(
                producto("1", "M", "Playera", 180.0, 1.0),
                producto("1", "L", "Playera", 180.0, 1.0)));
        assertEquals(2, r.size());
        assertEquals("M", r.get(0).getTalla());
        assertEquals("L", r.get(1).getTalla());
    }

    @Test
    void ignoraElementosQueNoSonObjetos() {
        List<Object> datos = new ArrayList<>();
        datos.add("texto");
        datos.add(5.0);
        datos.add(null);
        datos.add(producto("1", "", "Taza", 120.0, 1.0));
        assertEquals(1, CarritoService.limpiar(datos).size());
    }

    @Test
    void recortaIdNombreEImagenALosLimites() {
        Map<String, Object> m = producto("1".repeat(30), "", "N".repeat(200), 10.0, 1.0);
        m.put("imagen", "i".repeat(400));
        ItemCarrito item = CarritoService.limpiar(List.of(m)).get(0);
        assertEquals(20, item.getId().length());
        assertEquals(120, item.getNombre().length());
        assertEquals(255, item.getImagen().length());
    }

    @Test
    void convierteNumerosEscritosComoTextoYEnteroComoId() {
        ItemCarrito item = CarritoService.limpiar(List.of(producto(7.0, "", "Termo", "260.50", "3"))).get(0);
        assertEquals("7", item.getId());
        assertEquals(260.5, item.getPrecio());
        assertEquals(3, item.getCantidad());
    }

    @Test
    void guardarReemplazaElCarritoCompletoConDatosLimpios() throws SQLException {
        List<List<ItemCarrito>> guardado = new ArrayList<>();
        CarritoDAO dao = new CarritoDAO() {
            @Override public void asegurarTabla() { }
            @Override public void reemplazar(int idUsuario, List<ItemCarrito> items) { guardado.add(items); }
        };
        new CarritoService(dao).guardar(5, List.of(
                producto("1", "S", "Playera", 180.0, 1.0),
                producto("2", "ZZ", "Mala", 1.0, 1.0)));
        assertEquals(1, guardado.size());
        assertEquals(1, guardado.get(0).size());
    }
}
