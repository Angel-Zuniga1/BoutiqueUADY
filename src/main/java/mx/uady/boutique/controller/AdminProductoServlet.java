package mx.uady.boutique.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import mx.uady.boutique.dao.AdminDAO;
import mx.uady.boutique.dao.CategoriaDAO;
import mx.uady.boutique.util.JsonUtil;

/**
 * Solo administrador de sucursal (RF-09): productos DE SU SUCURSAL.
 * GET  : {sucursal, categorias, productos (con existencias aquí), otros (del catálogo, no en la sucursal)}
 * POST : formulario multipart con accion = crear | editar | baja | alta_existente
 *        (crear/editar aceptan el archivo "archivo" para la imagen)
 */
@WebServlet("/api/admin/productos")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
public class AdminProductoServlet extends AdminServletBase {

    private static final long serialVersionUID = 1L;
    private final AdminDAO dao = new AdminDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirSucursal(request, response)) {
            return;
        }
        int idSuc = AdminSesion.sucursal(request);
        try {
            Map<String, Object> m = mapa();
            m.put("sucursal", AdminSesion.sucursalNombre(request));
            m.put("categorias", categoriaDAO.listar());
            m.put("productos", dao.productosDeSucursal(idSuc));
            m.put("otros", dao.productosFueraDeSucursal(idSuc));
            JsonUtil.enviar(response, HttpServletResponse.SC_OK, m);
        } catch (SQLException e) {
            log("Error al leer productos de la sucursal", e);
            error(response, 500, "No se pudieron cargar los productos.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!exigirSucursal(request, response)) {
            return;
        }
        request.setCharacterEncoding("UTF-8");
        int idSuc = AdminSesion.sucursal(request);
        String nombreSuc = AdminSesion.sucursalNombre(request);
        String accion = param(request, "accion");
        try {
            if ("crear".equals(accion) || "editar".equals(accion)) {
                String nombre = param(request, "nombre");
                String descripcion = param(request, "descripcion");
                double precio = numero(param(request, "precio"));
                int idCategoria = (int) numero(param(request, "idCategoria"));
                String imagen = param(request, "imagen");
                if (nombre.isEmpty() || precio <= 0 || idCategoria <= 0) {
                    error(response, 400, "Nombre, precio mayor a cero y categoría son obligatorios.");
                    return;
                }
                String subida = guardarImagen(request);
                if (subida == null) {
                    error(response, 400, "La imagen debe ser JPG, PNG o WEBP.");
                    return;
                }
                if (!subida.isEmpty()) {
                    imagen = subida;
                }
                if ("crear".equals(accion)) {
                    int nuevo = dao.crearProducto(nombre, descripcion, precio, imagen, idCategoria);
                    // Inventario SOLO en la sucursal del administrador, con las existencias iniciales
                    if ("1".equals(param(request, "conTallas"))) {
                        for (String t : AdminDAO.TALLAS) {
                            dao.guardarFila(nuevo, idSuc, t, (int) numero(param(request, "inicial_" + t)));
                        }
                    } else {
                        dao.guardarFila(nuevo, idSuc, "", (int) numero(param(request, "inicialUnica")));
                    }
                    ok(response, "Producto \"" + nombre + "\" dado de alta en " + nombreSuc + ".");
                } else {
                    int id = (int) numero(param(request, "id"));
                    if (!dao.productoEnSucursal(id, idSuc)) {
                        error(response, 403, "Ese producto no está en tu sucursal.");
                        return;
                    }
                    dao.actualizarProducto(id, nombre, descripcion, precio, imagen, idCategoria);
                    ok(response, "Producto modificado.");
                }
            } else if ("baja".equals(accion)) {
                int id = (int) numero(param(request, "id"));
                if (!dao.productoEnSucursal(id, idSuc)) {
                    error(response, 403, "Ese producto no está en tu sucursal.");
                    return;
                }
                dao.quitarProductoDeSucursal(id, idSuc);
                ok(response, "Producto dado de baja en " + nombreSuc + ". Ya no se ofrece en esta sucursal.");
            } else if ("alta_existente".equals(accion)) {
                int id = (int) numero(param(request, "id"));
                dao.crearFilasEnCero(id, idSuc, "1".equals(param(request, "conTallas")));
                ok(response, "Producto agregado a tu sucursal. Captura sus existencias en Inventario.");
            } else {
                error(response, 400, "Acción no válida.");
            }
        } catch (SQLException e) {
            log("Error al guardar producto", e);
            error(response, 500, "No se pudo guardar el producto.");
        }
    }

    private static String param(HttpServletRequest request, String nombre) {
        String v = request.getParameter(nombre);
        return v == null ? "" : v.trim();
    }

    private static double numero(String v) {
        try {
            return v.isEmpty() ? 0 : Double.parseDouble(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Guarda la imagen subida en "fotos boutique/" de la aplicación desplegada.
     * @return ruta relativa de la imagen, "" si no se subió nada, null si el tipo no es válido
     */
    private String guardarImagen(HttpServletRequest request) throws IOException, ServletException {
        Part parte = request.getPart("archivo");
        if (parte == null || parte.getSize() == 0) {
            return "";
        }
        String tipo = parte.getContentType();
        String ext = "image/jpeg".equals(tipo) ? "jpg" : "image/png".equals(tipo) ? "png"
                : "image/webp".equals(tipo) ? "webp" : null;
        if (ext == null) {
            return null;
        }
        String nombre = "producto-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                + "-" + Integer.toHexString(new SecureRandom().nextInt(0xFFFFFF)) + "." + ext;
        File carpeta = new File(getServletContext().getRealPath("/fotos boutique"));
        carpeta.mkdirs();
        try (InputStream in = parte.getInputStream()) {
            Files.copy(in, new File(carpeta, nombre).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return "fotos boutique/" + nombre;
    }
}
