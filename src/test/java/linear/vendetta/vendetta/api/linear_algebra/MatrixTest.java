package linear.vendetta.vendetta.api.linear_algebra;

import static linear.vendetta.vendetta.api.TestUtils.EPS;
import static linear.vendetta.vendetta.api.TestUtils.assertMatrixClose;
import static linear.vendetta.vendetta.api.TestUtils.assertVectorClose;
import static linear.vendetta.vendetta.api.TestUtils.mat;
import static linear.vendetta.vendetta.api.TestUtils.row;
import static linear.vendetta.vendetta.api.TestUtils.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatrixTest {

    private final Matrix a = mat(row(1, 2), row(3, 4));

    // ---------- Construction ----------

    @Test
    void zeroMatrix() {
        Matrix m = new Matrix(2, 3);
        assertEquals(2, m.getNumRows());
        assertEquals(3, m.getNumCols());
        assertEquals(mat(row(0, 0, 0), row(0, 0, 0)), m);
    }

    @Test
    void invalidSizeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(0, 2));
    }

    @Test
    @DisplayName("build rejects rows of different sizes")
    void raggedRowsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Matrix.build(List.of(vec(1, 2), vec(3))));
    }

    @Test
    @DisplayName("The copy constructor makes an independent copy")
    void copyIsIndependent() {
        Matrix copy = new Matrix(a);
        copy.set(0, 0, 99);
        assertEquals(1.0, a.get(0, 0), EPS);
    }

    // ---------- Determinant ----------

    @Test
    void determinant2x2() {
        assertEquals(-2.0, a.getDeterminant(), EPS);
    }

    @Test
    void determinant3x3() {
        Matrix m = mat(row(2, 0, 1), row(1, 3, 2), row(1, 1, 2));
        assertEquals(6.0, m.getDeterminant(), EPS);
    }

    @Test
    @DisplayName("A singular matrix has determinant exactly 0")
    void singularMatrix() {
        Matrix m = mat(row(1, 2, 3), row(4, 5, 6), row(7, 8, 9));
        assertTrue(m.isSingular());
        assertEquals(0.0, m.getDeterminant());
    }

    @Test
    @DisplayName("A matrix of very small values is not singular (relative tolerance)")
    void smallValuesAreNotSingular() {
        Matrix m = mat(row(1e-6, 0), row(0, 1e-6));
        assertFalse(m.isSingular());
        assertEquals(1e-12, m.getDeterminant(), 1e-20);
    }

    @Test
    @DisplayName("The determinant updates after set()")
    void determinantCacheIsInvalidated() {
        Matrix m = new Matrix(a);
        assertEquals(-2.0, m.getDeterminant(), EPS);
        m.set(0, 0, 5);
        assertEquals(14.0, m.getDeterminant(), EPS);
    }

    @Test
    void nonSquareHasNoDeterminant() {
        assertThrows(IllegalArgumentException.class, () -> new Matrix(2, 3).getDeterminant());
    }

    // ---------- Arithmetic ----------

    @Test
    @DisplayName("add returns a new matrix and leaves both operands unchanged")
    void addDoesNotModifyOperands() {
        Matrix b = mat(row(5, 6), row(7, 8));
        assertEquals(mat(row(6, 8), row(10, 12)), a.add(b));
        assertEquals(mat(row(1, 2), row(3, 4)), a);
        assertEquals(mat(row(5, 6), row(7, 8)), b);
    }

    @Test
    void subtractAndScalarMult() {
        assertEquals(mat(row(0, 0), row(0, 0)), a.subtract(a));
        assertEquals(mat(row(2, 4), row(6, 8)), a.scalarMult(2));
    }

    @Test
    void addSizeMismatch() {
        assertThrows(IllegalArgumentException.class, () -> a.add(new Matrix(3, 3)));
    }

    @Test
    void transpose() {
        Matrix m = mat(row(1, 2, 3), row(4, 5, 6));
        assertEquals(mat(row(1, 4), row(2, 5), row(3, 6)), m.transpose());
    }

    @Test
    void multiplyMatrices() {
        assertEquals(mat(row(7, 10), row(15, 22)), Matrix.multiply(a, a));
    }

    @Test
    @DisplayName("(2x3) * (3x1) gives a 2x1 matrix")
    void multiplyRectangular() {
        Matrix m = mat(row(1, 2, 3), row(4, 5, 6));
        Matrix col = mat(row(1), row(0), row(-1));
        assertEquals(mat(row(-2), row(-2)), Matrix.multiply(m, col));
    }

    @Test
    void multiplyDimensionMismatch() {
        assertThrows(IllegalArgumentException.class, () -> Matrix.multiply(a, new Matrix(3, 2)));
    }

    @Test
    void multiplyMatrixByVector() {
        assertEquals(vec(17, 39), Matrix.multiply(a, vec(5, 6)));
    }

    // ---------- Adjugate, inverse, power ----------

    @Test
    void adjugate() {
        assertEquals(mat(row(4, -2), row(-3, 1)), Matrix.adj(a));
    }

    @Test
    @DisplayName("The adjugate of a 1x1 matrix is [1]")
    void adjugate1x1() {
        assertEquals(mat(row(1)), Matrix.adj(mat(row(7))));
    }

    @Test
    @DisplayName("inverse(A) * A = I")
    void inverseTimesMatrixIsIdentity() {
        Matrix m = mat(row(2, 0, 1), row(1, 3, 2), row(1, 1, 2));
        assertMatrixClose(Matrix.identity(3), Matrix.multiply(Matrix.inverse(m), m));
    }

    @Test
    void inverseOfSingularMatrixIsRejected() {
        assertThrows(ArithmeticException.class, () -> Matrix.inverse(mat(row(1, 2), row(2, 4))));
    }

    @Test
    void inverseOfNonSquareMatrixIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Matrix.inverse(new Matrix(2, 3)));
    }

    @Test
    void power() {
        assertEquals(Matrix.identity(2), Matrix.pow(a, 0));
        assertEquals(mat(row(37, 54), row(81, 118)), Matrix.pow(a, 3));
        assertMatrixClose(Matrix.inverse(a), Matrix.pow(a, -1));
    }

    @Test
    void nonIntegerPowerIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Matrix.pow(a, 1.5));
    }

    // ---------- Elimination and row operations ----------

    @Test
    @DisplayName("toTriangle returns an upper triangular matrix and keeps the original")
    void toTriangle() {
        Matrix m = mat(row(2, 1, 1), row(4, 3, 3), row(8, 7, 9));
        Matrix t = m.toTriangle();
        for (int r = 1; r < 3; r++) {
            for (int c = 0; c < r; c++) {
                assertEquals(0.0, t.get(r, c), EPS, "below the diagonal at (" + r + ", " + c + ")");
            }
        }
        assertEquals(mat(row(2, 1, 1), row(4, 3, 3), row(8, 7, 9)), m);
        assertFalse(m.getGaussSteps().isEmpty());
    }

    @Test
    @DisplayName("Row operations change the matrix in place and return the step text")
    void rowOperations() {
        Matrix m = new Matrix(a);
        assertEquals("R_0 <-> R_1\n", m.rowSwap(0, 1));
        assertEquals(mat(row(3, 4), row(1, 2)), m);

        assertEquals("R_1 -> R_1 * 2\n", m.rowMultiply(2, 1));
        assertEquals(mat(row(3, 4), row(2, 4)), m);

        assertEquals("R_1 -> R_1 + -1 * R_0\n", m.rowComp(1, 0, -1));
        assertEquals(mat(row(3, 4), row(-1, 0)), m);
    }

    @Test
    @DisplayName("A row swap flips the sign of the determinant")
    void rowSwapFlipsDeterminant() {
        Matrix m = new Matrix(a);
        m.rowSwap(0, 1);
        assertEquals(2.0, m.getDeterminant(), EPS);
    }

    @Test
    void identityAndConversions() {
        assertEquals(mat(row(1, 0), row(0, 1)), Matrix.identity(2));
        assertEquals(mat(row(5), row(6)), Matrix.convertToMatrix(vec(5, 6)));
        assertVectorClose(vec(5, 6), Vector.convertToVector(mat(row(5), row(6))));
    }
}
