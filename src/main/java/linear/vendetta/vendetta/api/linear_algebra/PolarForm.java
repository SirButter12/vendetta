package linear.vendetta.vendetta.api.linear_algebra;

import java.util.ArrayList;
import java.util.List;

/**
 * Polar (hyperspherical) form of a vector: length + n-1 angles in radians.
 * Display only: it cannot be added, multiplied, etc. Use toVector() for that.
 * In 1D there are no angles and length keeps the sign: [-5] -> {length: -5, angles: []}.
 * 
 * @Author: Ian
 */
public record PolarForm(double length, List<Double> angles) {

    public PolarForm {
        angles = List.copyOf(angles);
    }

    /** Back to Cartesian components. */
    public Vector toVector() {
        return Vector.fromPolar(length, angles);
    }

    @Override
    public String toString() {
        List<String> a = new ArrayList<>();
        for (double x : angles) {
            a.add(Vector.formatNumber(x));
        }
        return "{length: " + Vector.formatNumber(length) + ", angles: [" + String.join(", ", a) + "]}";
    }
}