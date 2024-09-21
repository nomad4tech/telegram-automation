package tech.nomad4.utils;

import java.util.concurrent.ThreadLocalRandom;

public class CommonUtils {

    private CommonUtils() { /* Prevent instantiation */ }

    public static void randomPause(Integer min, Integer max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(min, max));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
