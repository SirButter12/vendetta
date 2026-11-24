package linear.vendetta.vendetta.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.Vector;

/**
 * Shortcuts shared by every test class.
 * Vector.equals and Matrix.equals compare exactly, so results that come from
 * divisions are checked with a tolerance instead.
 */
public final class TestUtils {

    /** Tolerance for comparing computed doubles. */
    public static final double EPS = 1e-9;

    private TestUtils() { }

    /** vec(1, 2, 3) -> [1, 2, 3] */
    public static Vector vec(double... values) {
        List<Double> list = new ArrayList<>();
        for (double v : values) {
            list.add(v);
        }
        return new Vector(list);
    }

    /** mat(new double[]{1, 2}, new double[]{3, 4}) -> [[1, 2], [3, 4]] */
    public static Matrix mat(double[]... rows) {
        List<Vector> list = new ArrayList<>();
        for (double[] row : rows) {
            list.add(vec(row));
        }
        return Matrix.build(list);
    }

    /** Shorter way to write one row: row(1, 2). */
    public static double[] row(double... values) {
        return values;
    }

    public static void assertVectorClose(Vector expected, Vector actual) {
        assertEquals(expected.getDimension(), actual.getDimension(), "dimension");
        for (int i = 0; i < expected.getDimension(); i++) {
            assertEquals(expected.get(i), actual.get(i), EPS, "component " + i);
        }
    }

    public static void assertMatrixClose(Matrix expected, Matrix actual) {
        assertEquals(expected.getNumRows(), actual.getNumRows(), "rows");
        assertEquals(expected.getNumCols(), actual.getNumCols(), "columns");
        for (int r = 0; r < expected.getNumRows(); r++) {
            for (int c = 0; c < expected.getNumCols(); c++) {
                assertEquals(expected.get(r, c), actual.get(r, c), EPS, "entry (" + r + ", " + c + ")");
            }
        }
    }
}
