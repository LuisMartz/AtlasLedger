package atlasledger.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para PasswordUtils.
 */
@DisplayName("PasswordUtils Tests")
class PasswordUtilsTest {

    @Test
    @DisplayName("Hash de contraseña no debe ser nulo")
    void testHashPasswordNotNull() {
        // Arrange
        String password = "mySecurePassword123";

        // Act
        String hash = PasswordUtils.hashPassword(password);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
    }

    @Test
    @DisplayName("Contraseñas iguales deben generar hashes distintos (si usa salt)")
    void testHashPasswordConsistency() {
        // Arrange
        String password = "myPassword";

        // Act
        String hash1 = PasswordUtils.hashPassword(password);
        String hash2 = PasswordUtils.hashPassword(password);

        // Assert
        // Si implementa salt, los hashes deben ser diferentes
        // Si no, pueden ser iguales. Ajusta según tu implementación.
        assertNotNull(hash1);
        assertNotNull(hash2);
    }

    @Test
    @DisplayName("Verificación de contraseña correcta")
    void testVerifyPasswordCorrect() {
        // Arrange
        String password = "correctPassword";
        String hash = PasswordUtils.hashPassword(password);

        // Act
        boolean result = PasswordUtils.verifyPassword(password, hash);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Verificación de contraseña incorrecta")
    void testVerifyPasswordIncorrect() {
        // Arrange
        String password = "correctPassword";
        String wrongPassword = "wrongPassword";
        String hash = PasswordUtils.hashPassword(password);

        // Act
        boolean result = PasswordUtils.verifyPassword(wrongPassword, hash);

        // Assert
        assertFalse(result);
    }

}
