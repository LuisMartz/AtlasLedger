package atlasledger.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import atlasledger.model.Orden;
import atlasledger.model.Producto;
import atlasledger.model.Proveedor;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NetworkUtilsTest {

    @Test
    void mapToProductoBuildsDomainObject() {
        NetworkUtils utils = new NetworkUtils(Duration.ofSeconds(1));
        Map<String, Object> data = Map.of(
            "codigo", "P-1",
            "nombre", "Teclado",
            "categoria", "Perifericos",
            "proveedorCodigo", "PV-1",
            "stock", 10,
            "coste", 12.5,
            "precio", "25.0"
        );

        Producto producto = utils.mapToProducto(data);

        assertEquals("P-1", producto.getCodigo());
        assertEquals("Teclado", producto.getNombre());
        assertEquals("Perifericos", producto.getCategoria());
        assertEquals("PV-1", producto.getProveedorCodigo());
        assertEquals(10, producto.getStock());
        assertEquals(12.5, producto.getCoste());
        assertEquals(25.0, producto.getPrecio());
    }

    @Test
    void mapToProveedorBuildsDomainObject() {
        NetworkUtils utils = new NetworkUtils(Duration.ofSeconds(1));
        Map<String, Object> data = Map.of(
            "codigo", "PV-2",
            "nombre", "Proveedor SA",
            "email", "contacto@proveedor.com",
            "telefono", "111-222",
            "direccion", "Calle Uno 123"
        );

        Proveedor proveedor = utils.mapToProveedor(data);

        assertEquals("PV-2", proveedor.getCodigo());
        assertEquals("Proveedor SA", proveedor.getNombre());
        assertEquals("contacto@proveedor.com", proveedor.getEmail());
        assertEquals("111-222", proveedor.getTelefono());
        assertEquals("Calle Uno 123", proveedor.getDireccion());
    }

    @Test
    void mapToOrdenConvertsAndParsesValues() {
        NetworkUtils utils = new NetworkUtils(Duration.ofSeconds(1));
        Map<String, Object> data = Map.of(
            "codigo", "OC-1",
            "fecha", "2024-03-01",
            "proveedorCodigo", "PV-9",
            "total", "150.75",
            "estado", "APROBADA"
        );

        Orden orden = utils.mapToOrden(data);

        assertEquals("OC-1", orden.getCodigo());
        assertEquals("2024-03-01", orden.getFecha().toString());
        assertEquals("PV-9", orden.getProveedorCodigo());
        assertEquals(150.75, orden.getTotal());
        assertEquals(Orden.Estado.APROBADA, orden.getEstado());
    }

    @Test
    void defaultTimeoutIsExposed() {
        Duration timeout = Duration.ofSeconds(4);
        NetworkUtils utils = new NetworkUtils(timeout);
        assertEquals(timeout, utils.defaultTimeout());
        assertTrue(timeout.toMillis() > 0);
    }
}
