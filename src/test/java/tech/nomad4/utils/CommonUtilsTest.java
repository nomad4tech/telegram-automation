package tech.nomad4.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class CommonUtilsTest {
    @Test
    void testRandomPauseWithinRange() {
        assertTimeout(Duration.ofMillis(1500), () -> {
            CommonUtils.randomPause(500, 1500);
        });
    }

    @Test
    void testRandomPauseHandlesInterrupt() {
        Thread.currentThread().interrupt();
        assertThrows(RuntimeException.class, () -> CommonUtils.randomPause(500, 1000));
    }

    @Test
    void testRandomPauseWithMinEqualMax() {
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.randomPause(1000, 1000);
        });
    }

    @Test
    void testRandomPauseWithInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.randomPause(1500, 500);
        });
    }


}
