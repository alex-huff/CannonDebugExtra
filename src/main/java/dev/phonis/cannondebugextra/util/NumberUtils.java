package dev.phonis.cannondebugextra.util;

public final class NumberUtils {

    public static boolean isInsideCube(double coord) {
        double x = Math.abs(coord) % 1;

        return x >= 0.49000000953674316 && x <= 0.5099999904632568D;
    }

}
