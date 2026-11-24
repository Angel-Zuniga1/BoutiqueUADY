package mx.uady.boutique.model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Pedidos por estado y total vendido (sin cancelados) de una sucursal o de todas. */
public class ResumenVentas {

    public static final String[] ESTADOS = {"pendiente", "listo para recoger", "entregado", "cancelado"};

    private int pedidos;
    private double ventas;
    private Map<String, Integer> porEstado = new LinkedHashMap<>();

    public ResumenVentas() {
        for (String e : ESTADOS) {
            porEstado.put(e, 0);
        }
    }

    public int getPedidos() { return pedidos; }
    public void setPedidos(int pedidos) { this.pedidos = pedidos; }

    public double getVentas() { return ventas; }
    public void setVentas(double ventas) { this.ventas = ventas; }

    public Map<String, Integer> getPorEstado() { return porEstado; }

    public int pendientes() { return porEstado.getOrDefault("pendiente", 0); }

    public static boolean esEstadoValido(String estado) {
        for (String e : ESTADOS) {
            if (e.equals(estado)) {
                return true;
            }
        }
        return false;
    }
}
