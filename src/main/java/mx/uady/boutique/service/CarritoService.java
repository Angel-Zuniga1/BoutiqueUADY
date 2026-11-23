package mx.uady.boutique.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mx.uady.boutique.dao.CarritoDAO;
import mx.uady.boutique.model.ItemCarrito;

/**
 * Reglas de negocio del carrito por usuario: validación y limpieza de los datos que llegan
 * del navegador (mismas reglas que carrito_api.php) y lectura/guardado en la base de datos.
 */
public class CarritoService {

    /** Tallas permitidas; la cadena vacía es para productos sin talla (accesorios). */
    public static final List<String> TALLAS_VALIDAS = List.of("", "XS", "S", "M", "L", "XL", "2XL");

    public static final int CANTIDAD_MAXIMA = 99;
    private static final int MAX_ID = 20;
    private static final int MAX_NOMBRE = 120;
    private static final int MAX_IMAGEN = 255;

    private static final Pattern NUMERO_INICIAL =
            Pattern.compile("^\\s*([+-]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?)");

    private final CarritoDAO carritoDAO;

    public CarritoService() {
        this(new CarritoDAO());
    }

    public CarritoService(CarritoDAO carritoDAO) {
        this.carritoDAO = carritoDAO;
    }

    /** Carrito guardado del usuario. */
    public List<ItemCarrito> obtener(int idUsuario) throws SQLException {
        carritoDAO.asegurarTabla();
        return carritoDAO.listar(idUsuario);
    }

    /**
     * Reemplaza el carrito del usuario con los datos recibidos (ya limpiados y validados).
     *
     * @param datos lista de productos tal como llegó en el JSON
     */
    public void guardar(int idUsuario, List<?> datos) throws SQLException {
        carritoDAO.asegurarTabla();
        carritoDAO.reemplazar(idUsuario, limpiar(datos));
    }

    /**
     * Valida y limpia cada producto: descarta los inválidos, limita la cantidad a 99 y suma
     * las líneas repetidas (mismo producto y misma talla).
     */
    public static List<ItemCarrito> limpiar(List<?> datos) {
        Map<String, ItemCarrito> limpios = new LinkedHashMap<>();
        for (Object elemento : datos) {
            if (!(elemento instanceof Map)) {
                continue;
            }
            Map<?, ?> item = (Map<?, ?>) elemento;

            String id = truncar(aTexto(item.get("id")).trim(), MAX_ID);
            String talla = aTexto(item.get("talla"));
            String nombre = truncar(aTexto(item.get("nombre")).trim(), MAX_NOMBRE);
            String imagen = truncar(aTexto(item.get("imagen")), MAX_IMAGEN);
            double precio = aDecimal(item.get("precio"));
            int cantidad = aEntero(item.get("cantidad"));

            if (id.isEmpty() || nombre.isEmpty() || precio < 0 || cantidad < 1) {
                continue;
            }
            if (!TALLAS_VALIDAS.contains(talla)) {
                continue;
            }
            if (cantidad > CANTIDAD_MAXIMA) {
                cantidad = CANTIDAD_MAXIMA;
            }

            String clave = id + "|" + talla;
            ItemCarrito existente = limpios.get(clave);
            if (existente != null) {
                existente.setCantidad(Math.min(CANTIDAD_MAXIMA, existente.getCantidad() + cantidad));
            } else {
                limpios.put(clave, new ItemCarrito(id, talla, nombre, precio, imagen, cantidad));
            }
        }
        return new ArrayList<>(limpios.values());
    }

    // ---------- conversiones tolerantes (el JSON viene del navegador) ----------

    static String aTexto(Object valor) {
        if (valor == null) {
            return "";
        }
        if (valor instanceof Number) {
            double d = ((Number) valor).doubleValue();
            if (d == Math.rint(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
                return Long.toString((long) d);
            }
            return valor.toString();
        }
        if (valor instanceof Boolean) {
            return ((Boolean) valor) ? "1" : "";
        }
        if (valor instanceof CharSequence) {
            return valor.toString();
        }
        return "";
    }

    static double aDecimal(Object valor) {
        if (valor instanceof Number) {
            return ((Number) valor).doubleValue();
        }
        if (valor instanceof Boolean) {
            return ((Boolean) valor) ? 1 : 0;
        }
        if (valor instanceof CharSequence) {
            Matcher m = NUMERO_INICIAL.matcher(valor.toString());
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    static int aEntero(Object valor) {
        double d = aDecimal(valor);
        if (Double.isNaN(d)) {
            return 0;
        }
        if (d >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (d <= Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) d;
    }

    /** Recorta a {@code max} caracteres (por puntos de código, para no partir emojis). */
    static String truncar(String texto, int max) {
        if (texto.codePointCount(0, texto.length()) <= max) {
            return texto;
        }
        return texto.substring(0, texto.offsetByCodePoints(0, max));
    }
}
