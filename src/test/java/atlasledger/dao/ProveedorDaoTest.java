package atlasledger.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para ProveedorDao.
 * 
 * Notas: Estos tests son especulativos y requieren que la clase ProveedorDao
 * esté completamente implementada.
 */
@DisplayName("ProveedorDao Tests")
class ProveedorDaoTest {

    private ProveedorDao proveedorDao;

    @BeforeEach
    void setUp() {
        proveedorDao = mock(ProveedorDao.class);
    }

    @Test
    @DisplayName("Crear un nuevo proveedor")
    void testCrearProveedor() {
        // Arrange
        String nombre = "Proveedor Test";
        String contacto = "contacto@proveedor.com";
        String ubicacion = "Ciudad X";

        // Act & Assert
        assertDoesNotThrow(() -> {
            proveedorDao.crear(nombre, contacto, ubicacion);
        });
    }

    @Test
    @DisplayName("Obtener todos los proveedores")
    void testObtenerTodos() {
        // Arrange
        when(proveedorDao.obtenerTodos()).thenReturn(java.util.Collections.emptyList());

        // Act
        var proveedores = proveedorDao.obtenerTodos();

        // Assert
        assertNotNull(proveedores);
        assertTrue(proveedores.isEmpty());
    }

    @Test
    @DisplayName("Obtener proveedor por ID")
    void testObtenerPorId() {
        // Arrange
        int id = 1;

        // Act & Assert
        assertDoesNotThrow(() -> proveedorDao.obtenerPorId(id));
    }

    @Test
    @DisplayName("Actualizar un proveedor")
    void testActualizarProveedor() {
        // Arrange
        int id = 1;
        String nuevoNombre = "Proveedor Actualizado";
        String nuevoContacto = "nuevo@contacto.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            proveedorDao.actualizar(id, nuevoNombre, nuevoContacto);
        });
    }

    @Test
    @DisplayName("Eliminar un proveedor")
    void testEliminarProveedor() {
        // Arrange
        int id = 1;

        // Act & Assert
        assertDoesNotThrow(() -> proveedorDao.eliminar(id));
    }

}
