package atlasledger.utils;

import atlasledger.model.Orden;
import atlasledger.model.Producto;
import atlasledger.model.Proveedor;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NetworkUtils {

    private final Duration defaultTimeout;

    public NetworkUtils(Duration defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    public Duration defaultTimeout() {
        return defaultTimeout;
    }

    public boolean isOnline() {
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            return address.isReachable((int) defaultTimeout.toMillis());
        } catch (Exception e) {
            Logger.warn(NetworkUtils.class, "Sin conectividad de red disponible.");
            return false;
        }
    }

    /**
     * Analiza una respuesta JSON en formato lista. Este metodo es un stub a la espera
     * de integrar una libreria JSON real; por ahora devuelve una lista vacia para
     * mantener la arquitectura lista para ampliarse.
     */
    public List<Map<String, Object>> parseJsonArray(String body) {
        Logger.warn(NetworkUtils.class, "Parser JSON no implementado. Respuesta ignorada.");
        return Collections.emptyList();
    }

    public Producto mapToProducto(Map<String, Object> data) {
        Producto producto = new Producto();
        producto.setCodigo(asString(data.get("codigo")));
        producto.setNombre(asString(data.get("nombre")));
        producto.setCategoria(asString(data.get("categoria")));
        producto.setProveedorCodigo(asString(data.get("proveedorCodigo")));
        producto.setStock(asInt(data.get("stock")));
        producto.setCoste(asDouble(data.get("coste")));
        producto.setPrecio(asDouble(data.get("precio")));
        return producto;
    }

    public Proveedor mapToProveedor(Map<String, Object> data) {
        Proveedor proveedor = new Proveedor();
        proveedor.setCodigo(asString(data.get("codigo")));
        proveedor.setNombre(asString(data.get("nombre")));
        proveedor.setEmail(asString(data.get("email")));
        proveedor.setTelefono(asString(data.get("telefono")));
        proveedor.setDireccion(asString(data.get("direccion")));
        return proveedor;
    }

    public Orden mapToOrden(Map<String, Object> data) {
        Orden orden = new Orden();
        orden.setCodigo(asString(data.get("codigo")));
        String fecha = asString(data.get("fecha"));
        if (!fecha.isEmpty()) {
            orden.setFecha(java.time.LocalDate.parse(fecha));
        }
        orden.setProveedorCodigo(asString(data.get("proveedorCodigo")));
        orden.setTotal(asDouble(data.get("total")));
        String estado = asString(data.get("estado"));
        if (!estado.isEmpty()) {
            orden.setEstado(Orden.Estado.valueOf(estado));
        }
        return orden;
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private int asInt(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(asString(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(asString(value));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
