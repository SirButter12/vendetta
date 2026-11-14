package linear.vendetta.vendetta.api.linear_algebra;
 
import java.util.ArrayList;
import java.util.List;
 
/**
 * Linear system Ax = b: this matrix is A, eqVector is b.
 * b is never modified; solving works on a copy.
 *
 * @author ian
 */
public class SystemMatrix extends Matrix{
    private TransformableVector eqVector;   // b, the right-hand side
 
    /**
     * System from A (copied) and b (copied).
     *
     * @throws IllegalArgumentException if b size != number of rows of A
     */
    public SystemMatrix(Matrix m, Vector v) {
        super(m);
 
        if (v == null || v.getDimension() != m.getNumRows()) {
            throw new IllegalArgumentException("The vector must have one value per row of the matrix ("
                    + m.getNumRows() + ").");
        }
 
        eqVector = new TransformableVector(v);
    }
 
    /**
     * Solves Ax = b with Gauss-Jordan elimination.
     * The row operations are saved in gaussSteps.
     *
     * @return the unique solution x
     * @throws MultipleOrNoSolutionException if there is no solution or infinitely many
     */
    public SolutionVector gaussJordan() {
        int m = getNumRows();
        int n = getNumCols();
 
        // 1) Triangulate A, applying the same row ops to a copy of b (eqVector is never modified)
        TransformableVector b = new TransformableVector(eqVector);
        gaussSteps.clear();
        Elimination e = eliminate(b, gaussSteps);
        detSign = e.sign();
        Matrix triang = e.triangle();
 
        // Relative tolerances (as in Matrix), not a fixed 0.00001.
        // tolA: "zero" for A. tolB: "zero" for b, scaled by b's own size.
        double tolA = pivotTolerance();
        double bScale = 0.0;
        for (int r = 0; r < m; r++) {
            bScale = Math.max(bScale, Math.abs(eqVector.get(r)));
        }
        double tolB = Math.max(tolA, bScale * Math.max(m, n) * REL_EPS);
 
        // 2) A zero row with b != 0 reads "0 = c": no solution.
        for (int r = 0; r < m; r++) {
            if (isZeroRow(triang, r, tolA) && Math.abs(b.get(r)) > tolB) {
                throw new MultipleOrNoSolutionException("The system has no solution (row " + r
                        + " reads 0 = " + Vector.formatNumber(b.get(r)) + ").");
            }
        }
 
        // 3) Unique solution needs a pivot in every column; otherwise a variable is free.
        if (m < n) {
            throw new MultipleOrNoSolutionException("The system has infinitely many solutions ("
                    + n + " unknowns but only " + m + " equations).");
        }
        for (int i = 0; i < n; i++) {
            if (Math.abs(triang.get(i, i)) <= tolA) {
                throw new MultipleOrNoSolutionException(
                        "The system has infinitely many solutions (x_" + i + " is free).");
            }
        }
 
        // 4) Back-substitution, last column first: make the pivot 1, then clear above it.
        for (int col = n - 1; col >= 0; col--) {
            double pivot = triang.get(col, col);
            if (pivot != 1.0) {
                gaussSteps.add(triang.rowMultiply(1.0 / pivot, col));
                b.rowMultiply(1.0 / pivot, col);
                triang.rows.get(col).set(col, 1.0);
            }
 
            for (int r = 0; r < col; r++) {
                double factor = triang.get(r, col);
                if (factor != 0.0) {
                    gaussSteps.add(triang.rowComp(r, col, -factor));
                    b.rowComp(r, col, -factor);
                    triang.rows.get(r).set(col, 0.0);
                }
            }
        }
 
        // 5) A is now the identity, so b holds x.
        List<Double> solution = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            solution.add(b.get(i));
        }
        
        return new SolutionVector(new Vector(solution), this.gaussSteps);
    }
 
    /** True if every value in the row has |value| at or below tol. */
    private static boolean isZeroRow(Matrix mat, int row, double tol) {
        for (int c = 0; c < mat.getNumCols(); c++) {
            if (Math.abs(mat.get(row, c)) > tol) {
                return false;
            }
        }
        return true;
    }
 
    // toTriangle() is no longer overridden: the Matrix version (which uses
    // eliminate()) is enough. The old override modified eqVector, and since
    // calcDeterminant() called it, a simple set() scrambled b.
 
    /**
     * Solves Ax = b as x = inverse(A) * b. Square systems only.
     * Can't tell "no solution" from "infinitely many"; use gaussJordan() for that.
     *
     * @throws IllegalArgumentException if A is not square
     * @throws MultipleOrNoSolutionException if A is singular
     */
    public Vector solve() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Only square systems can be solved with the inverse.");
        }
 
        if (isSingular()) {     // not getDeterminant() == 0, see Matrix.isSingular()
            throw new MultipleOrNoSolutionException(
                    "The matrix is singular (det = 0): the system has no solution or infinitely many."
                    + " Use gaussJordan to find out which.");
        }
 
 
        return multiply(inverse(this), eqVector);
    }
 
    /** Copy of b. */
    public Vector getEqVector() {
        return new Vector(eqVector);
    }
 
    /** Same A and same b. */
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        return eqVector.equals(((SystemMatrix) o).eqVector);
    }
 
    /** Consistent with equals (A and b). */
    @Override
    public int hashCode() {
        return 31 * super.hashCode() + eqVector.hashCode();
    }
 
}
 
