package atlasledger.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para DBHelper.
 * 
 * Notas: Estos tests requieren que DBHelper maneje conexiones de BD.
 * Pueden requerir una BD de prueba o mocks.
 */
@DisplayName("DBHelper Tests")
class DBHelperTest {

    private DBHelper dbHelper;

    @BeforeEach
    void setUp() {
        // Inicializar con BD de prueba o mock
        dbHelper = DBHelper.getInstance();
    }

    @Test
    @DisplayName("Singleton instance no es nulo")
    void testGetInstance() {
        // Act & Assert
        assertNotNull(dbHelper);
    }

    @Test
    @DisplayName("Obtener conexión no es nula")
    void testGetConnection() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            var connection = dbHelper.getConnection();
            assertNotNull(connection);
        });
    }

    @Test
    @DisplayName("Conexión es válida")
    void testConnectionIsValid() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            var connection = dbHelper.getConnection();
            assertFalse(connection.isClosed());
        });
    }

    @Test
    @DisplayName("Override de ruta de BD funciona")
    void testOverrideDatabasePath() {
        // Arrange
        String newPath = "test.db";

        // Act & Assert
        assertDoesNotThrow(() -> {
            dbHelper.overrideDatabasePath(newPath);
        });
    }

}
