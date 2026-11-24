package linear.vendetta.vendetta.api.linear_algebra;

import static linear.vendetta.vendetta.api.TestUtils.EPS;
import static linear.vendetta.vendetta.api.TestUtils.assertVectorClose;
import static linear.vendetta.vendetta.api.TestUtils.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VectorTest {

    // ---------- Construction ----------

    @Test
    @DisplayName("The list constructor copies the list")
    void listConstructorCopiesTheList() {
        List<Double> values = new ArrayList<>(List.of(1.0, 2.0));
        Vector v = new Vector(values);
        values.set(0, 99.0);
        assertEquals(1.0, v.get(0), EPS);
    }

    @Test
    @DisplayName("An empty list is rejected")
    void emptyListIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Vector(new ArrayList<Double>()));
    }

    @Test
    @DisplayName("Vector(n) creates a zero vector of dimension n")
    void zeroVector() {
        Vector v = new Vector(3);
        assertEquals(3, v.getDimension());
        assertEquals(vec(0, 0, 0), v);
    }

    @Test
    @DisplayName("Vector(0) is rejected")
    void dimensionZeroIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Vector(0));
    }

    @Test
    @DisplayName("The copy constructor makes an independent copy")
    void copyIsIndependent() {
        Vector original = vec(1, 2);
        Vector copy = new Vector(original);
        copy.set(0, 50);
        assertEquals(1.0, original.get(0), EPS);
    }

    // ---------- Arithmetic ----------

    @Test
    @DisplayName("add returns a new vector and leaves both operands unchanged")
    void addDoesNotModifyOperands() {
        Vector a = vec(1, 2);
        Vector b = vec(3, 4);
        assertEquals(vec(4, 6), a.add(b));
        assertEquals(vec(1, 2), a);
        assertEquals(vec(3, 4), b);
    }

    @Test
    void subtract() {
        assertEquals(vec(-2, -2), vec(1, 2).subtract(vec(3, 4)));
    }

    @Test
    void scalarMult() {
        assertEquals(vec(3, -6), vec(1, -2).scalarMult(3));
    }

    @Test
    @DisplayName("Adding vectors of different dimensions is rejected")
    void addDimensionMismatch() {
        assertThrows(IllegalArgumentException.class, () -> vec(1, 2).add(vec(1, 2, 3)));
    }

    @Test
    void dotProduct() {
        assertEquals(32.0, vec(1, 2, 3).dotProduct(vec(4, 5, 6)), EPS);
    }

    @Test
    void dotProductDimensionMismatch() {
        assertThrows(IllegalArgumentException.class, () -> vec(1, 2).dotProduct(vec(1, 2, 3)));
    }

    // ---------- Geometry ----------

    @Test
    @DisplayName("cross2D of i and j is 1")
    void cross2D() {
        assertEquals(1.0, vec(1, 0).cross2D(vec(0, 1)), EPS);
        assertEquals(-1.0, vec(0, 1).cross2D(vec(1, 0)), EPS);
    }

    @Test
    void cross2DRejects3DVectors() {
        assertThrows(IllegalArgumentException.class, () -> vec(1, 0, 0).cross2D(vec(0, 1, 0)));
    }

    @Test
    @DisplayName("i x j = k")
    void cross3D() {
        assertEquals(vec(0, 0, 1), vec(1, 0, 0).cross3D(vec(0, 1, 0)));
    }

    @Test
    @DisplayName("cross3D is orthogonal to both vectors")
    void cross3DIsOrthogonal() {
        Vector a = vec(1, 2, 3);
        Vector b = vec(-2, 0, 5);
        Vector c = a.cross3D(b);
        assertEquals(0.0, c.dotProduct(a), EPS);
        assertEquals(0.0, c.dotProduct(b), EPS);
    }

    @Test
    void projectOn() {
        assertVectorClose(vec(3, 0), vec(3, 4).projectOn(vec(1, 0)));
        assertVectorClose(vec(1, 1), vec(2, 0).projectOn(vec(1, 1)));
    }

    @Test
    @DisplayName("Projecting onto the zero vector is rejected")
    void projectOnZeroVector() {
        assertThrows(ArithmeticException.class, () -> vec(1, 2).projectOn(vec(0, 0)));
    }

    @Test
    void length() {
        assertEquals(5.0, vec(3, 4).getLen(), EPS);
    }

    @Test
    @DisplayName("Polar form converts back to the same vector")
    void polarRoundTrip() {
        Vector v = vec(1, -2, 3);
        assertVectorClose(v, v.toPolar().toVector());
    }

    @Test
    @DisplayName("angleInBet of perpendicular and parallel vectors")
    void angleInBetSameDimension() {
        assertEquals(Math.PI / 2, Vector.angleInBet(vec(1, 0), vec(0, 1)), EPS);
        assertEquals(0.0, Vector.angleInBet(vec(1, 1), vec(2, 2)), EPS);
        assertEquals(Math.PI, Vector.angleInBet(vec(1, 0), vec(-3, 0)), EPS);
    }

    @Test
    @DisplayName("angleInBet pads the smaller vector with zeros: [1, 2] vs [1, 2, 3] = [1, 2, 0] vs [1, 2, 3]")
    void angleInBetDifferentDimensions() {
        double expected = Vector.angleInBet(vec(1, 2, 0), vec(1, 2, 3));
        assertEquals(expected, Vector.angleInBet(vec(1, 2), vec(1, 2, 3)), EPS);
        assertEquals(expected, Vector.angleInBet(vec(1, 2, 3), vec(1, 2)), EPS);   // order does not matter
        assertEquals(Math.PI / 2, Vector.angleInBet(vec(1), vec(0, 0, 5)), EPS);
    }

    @Test
    @DisplayName("angleInBet does not modify its arguments")
    void angleInBetKeepsOriginals() {
        Vector small = vec(1, 2);
        Vector.angleInBet(small, vec(1, 2, 3));
        assertEquals(2, small.getDimension());
    }

    @Test
    @DisplayName("The angle with the zero vector is undefined")
    void angleInBetZeroVector() {
        assertThrows(ArithmeticException.class, () -> Vector.angleInBet(vec(0, 0), vec(1, 2, 3)));
    }

    @Test
    @DisplayName("padTo adds zeros at the end and keeps the original")
    void padTo() {
        Vector v = vec(1, 2);
        assertEquals(vec(1, 2, 0, 0), v.padTo(4));
        assertEquals(vec(1, 2), v.padTo(2));
        assertEquals(vec(1, 2), v);
        assertThrows(IllegalArgumentException.class, () -> v.padTo(1));
    }

    // ---------- Utility ----------

    @Test
    @DisplayName("-0 equals 0")
    void negativeZeroEqualsZero() {
        assertEquals(vec(0, 1), vec(-0.0, 1));
        assertEquals(vec(0, 1).hashCode(), vec(-0.0, 1).hashCode());
    }

    @Test
    void toStringFormat() {
        assertEquals("[1, 2.5, -3]", vec(1, 2.5, -3).toString());
    }
}
