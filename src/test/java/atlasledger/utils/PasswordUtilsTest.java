package atlasledger.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilsTest {

    @Test
    void hashIsDeterministicAndHex() {
        String hash = PasswordUtils.hash("secret");
        assertEquals(64, hash.length());
        assertEquals(hash, PasswordUtils.hash("secret"));
    }

    @Test
    void matchesComparesRawAgainstHash() {
        String hash = PasswordUtils.hash("admin");
        assertTrue(PasswordUtils.matches("admin", hash));
        assertFalse(PasswordUtils.matches("wrong", hash));
        assertFalse(PasswordUtils.matches("admin", null));
        assertNotEquals("admin", hash);
    }
}
