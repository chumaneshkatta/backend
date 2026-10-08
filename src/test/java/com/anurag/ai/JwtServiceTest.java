package com.anurag.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.anurag.ai.model.User;
import com.anurag.ai.service.JwtService;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = "unit-test-secret-that-is-at-least-32-bytes-long";

    private User user(long id) {
        User u = new User();
        try {
            var f = User.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(u, id);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
        return u;
    }

    @Test void roundTrip() {
        JwtService jwt = new JwtService(SECRET, 60, false);
        assertEquals(7L, jwt.parseUserId(jwt.generate(user(7))).orElseThrow());
    }

    @Test void tamperedTokenRejected() {
        JwtService jwt = new JwtService(SECRET, 60, false);
        assertTrue(jwt.parseUserId(jwt.generate(user(7)) + "x").isEmpty());
        assertTrue(jwt.parseUserId("not-a-jwt").isEmpty());
    }

    @Test void tokenSignedWithOtherKeyRejected() {
        JwtService other = new JwtService("another-secret-another-secret-another-secret", 60, false);
        assertTrue(new JwtService(SECRET, 60, false).parseUserId(other.generate(user(1))).isEmpty());
    }

    @Test void expiredTokenRejected() {
        JwtService jwt = new JwtService(SECRET, -1, false);
        assertTrue(jwt.parseUserId(jwt.generate(user(1))).isEmpty());
    }

    @Test void weakSecretRefused() {
        assertThrows(IllegalStateException.class, () -> new JwtService("short", 60, false));
    }

    @Test void missingSecretRefusedInProductionButRandomInDev() {
        assertThrows(IllegalStateException.class, () -> new JwtService("", 60, true));
        JwtService dev = new JwtService("", 60, false);
        assertEquals(3L, dev.parseUserId(dev.generate(user(3))).orElseThrow());
    }
}
