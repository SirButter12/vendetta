package linear.vendetta.vendetta.API;

import static java.lang.Double.NaN;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * m x n matrix of doubles, stored as a list of row Vectors.
 *
 * - Determinant: lazy. Computed the first time it is asked for, then cached
 *   until the rows change (NaN if not square).
 * - Gauss elimination lives in ONE place: eliminate(). toTriangle(),
 *   calcDeterminant() and SystemMatrix all use it.
 * - "Zero" uses a relative tolerance (see REL_EPS), not a fixed number.
 * - Arithmetic returns NEW matrices; set*, row* methods change this one.
 *
 * @author Matthew, Ian, Adrian
 */
public class Matrix implements operable<Matrix>, elementalOperations {
    protected List<Vector> rows;                            // one Vector per row
    protected int[] size = new int[2];                      // {numRows, numCols}
    protected double detSign;                               // +1/-1 from row swaps in the last toTriangle()
    protected List<String> gaussSteps = new ArrayList<>();  // row ops of the last toTriangle()/gaussJordan()

    /**
     * Relative tolerance for "is this zero?". Real limit = biggest |value| * max(rows, cols) * REL_EPS,
     * so it scales with the numbers: a matrix of 0.000001s is NOT singular,
     * but a 1e-15 rounding leftover (e.g. in [[1,2,3],[4,5,6],[7,8,9]]) IS zero.
     */
    protected static final double REL_EPS = 1e-12;

    // Determinant cache. Any change to the rows sets detValid = false.
    private double determinant;         // valid only if detValid
    private boolean singular;           // true if some column had no pivot (det = 0)
    private boolean detValid = false;

    /**
     * Result of eliminate().
     *
     * @param triangle the row echelon form (a new matrix)
     * @param sign     +1 or -1, from the row swaps
     * @param singular true if some column had no pivot
     */
    protected record Elimination(Matrix triangle, double sign, boolean singular) { }



    //---CONSTRUCTOR METHODS AND DETERMINANT---


    /**
     * Use build(). Copies the rows. Determinant is not computed yet (lazy).
     *
     * @throws IllegalArgumentException if empty or rows have different sizes
     */
    private Matrix(List<Vector> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("A matrix needs at least 1 row.");
        }

        int cols = Objects.requireNonNull(rows.get(0), "A matrix cannot contain null rows.").getDimension();

        this.rows = new ArrayList<>();
        for (Vector row : rows) {
            Objects.requireNonNull(row, "A matrix cannot contain null rows.");
            if (row.getDimension() != cols) {
                throw new IllegalArgumentException("Every row must have " + cols
                        + " columns, but one has " + row.getDimension() + ".");
            }
            this.rows.add(new Vector(row));
        }

        this.size[0] = rows.size();
        this.size[1] = cols;
    }


    //Ian's stuff - pushed by Matthew
    /**
     * Zero matrix with n rows and m columns.
     *
     * @throws IllegalArgumentException if n or m is less than 1
     */
    public Matrix(int n, int m) {
        if (n <= 0 || m <= 0) {
            throw new IllegalArgumentException("A matrix needs at least 1 row and 1 column");
        }

        this.rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            this.rows.add(new Vector(m));
        }

        this.size = new int[]{n, m};
    }

    /** Deep copy (rows, size and determinant cache). Does not copy gaussSteps or detSign. */
    public Matrix(Matrix m) {
        this.rows = new ArrayList<>();
        for (Vector r : m.rows) this.rows.add(new Vector(r));
        this.size = m.size.clone();
        this.determinant = m.determinant;
        this.singular = m.singular;
        this.detValid = m.detValid;
    }

    /**
     * Matrix from a list of rows (each Vector = one row). Rows are copied.
     *
     * @throws IllegalArgumentException if the list is empty or rows differ in size
     */
    public static Matrix build(List<Vector> rows) {
        return new Matrix(rows);
    }

    /**
     * Recomputes the determinant and the singular flag now.
     * det = product of the triangle's diagonal * sign; exactly 0 if singular, NaN if not square.
     * No side effects: does not touch gaussSteps, detSign or a SystemMatrix's b.
     * Normally you don't call this; getDeterminant() calls it when needed.
     */
    public void calcDeterminant() {
        detValid = true;
        if (size[0] != size[1]) {
            determinant = NaN;
            singular = false;
            return;
        }

        Elimination e = eliminate(null, null);
        singular = e.singular();
        if (singular) {
            determinant = 0.0;      // exact 0, not a rounding leftover like 1e-15
            return;
        }

        double total = e.sign();
        for (int i = 0; i < size[0]; i++) {
            total *= e.triangle().get(i, i);
        }
        determinant = total;
    }

    /** Computes the determinant only if the cache is stale. */
    private void ensureDeterminant() {
        if (!detValid) {
            calcDeterminant();
        }
    }

    /** Marks the cached determinant as stale. Call after ANY change to the rows. */
    protected void invalidateDeterminant() {
        detValid = false;
    }

    /** Pivots with |value| at or below this count as zero: biggest |value| * max(rows, cols) * REL_EPS. */
    protected double pivotTolerance() {
        double scale = 0.0;
        for (Vector row : rows) {
            for (int c = 0; c < size[1]; c++) {
                scale = Math.max(scale, Math.abs(row.get(c)));
            }
        }
        return scale * Math.max(size[0], size[1]) * REL_EPS;
    }

    /**
     * True if the matrix has no inverse (det = 0, allowing for rounding).
     * Use this instead of getDeterminant() == 0.
     *
     * @throws IllegalArgumentException if not square
     */
    public boolean isSingular() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Only square matrices can be singular or invertible, this one is "
                    + size[0] + "x" + size[1] + ".");
        }
        ensureDeterminant();
        return singular;
    }


    //---MATRIX FUNCTIONS METHODS---

    /**
     * Inverse: adj(m) / det(m).
     *
     * @throws IllegalArgumentException if not square
     * @throws ArithmeticException if singular
     */
    public static Matrix inverse(Matrix m) {
        if (m.getNumRows() != m.getNumCols()) {
            throw new IllegalArgumentException("Only square matrices have an inverse.");
        }

        if (m.isSingular()) {   // not det == 0: rounding can leave det = 1e-15 instead of 0
            throw new ArithmeticException("The matrix is singular (det = 0), so it has no inverse.");
        }

        return Matrix.adj(m).scalarMult(1.0 / m.getDeterminant());
    }

    /**
     * Adjugate: transposed cofactor matrix. adj of a 1x1 is [1].
     *
     * @throws IllegalArgumentException if not square
     */
    public static Matrix adj(Matrix m) {
        int n = m.getNumRows();
        if (n != m.getNumCols()) {
            throw new IllegalArgumentException("The adjugate only exists for square matrices.");
        }

        if (n == 1) {
            return identity(1);
        }

        Matrix result = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double minor = m.getSubMatrix(i, j).getDeterminant();
                double sign = ((i + j) % 2 == 0) ? 1.0 : -1.0;

                result.rows.get(j).set(i, sign * minor);   // stored at (j, i) = transposed
            }
        }

        return result;
    }

    /** Copy without row i and column j (used for minors/cofactors). */
    private Matrix getSubMatrix(int i, int j) {
        if (i < 0 || i >= size[0] || j < 0 || j >= size[1]) {
            throw new IndexOutOfBoundsException(
                    "Position (" + i + ", " + j + ") is outside a "
                    + size[0] + "x" + size[1] + " matrix.");
        }
        if (size[0] < 2 || size[1] < 2) {
            throw new IllegalArgumentException("A submatrix needs a matrix of at least 2x2.");
        }

        List<Vector> subRows = new ArrayList<>();
        for (int r = 0; r < size[0]; r++) {
            if (r == i) continue;                       // skip row i

            List<Double> values = new ArrayList<>();
            for (int c = 0; c < size[1]; c++) {
                if (c == j) continue;                   // skip column j
                values.add(get(r, c));
            }
            subRows.add(new Vector(values));
        }

        return build(subRows);
    }

    /**
     * Row echelon form (upper triangle). Returns a NEW matrix; this one keeps its values.
     * Side effect: fills gaussSteps and detSign.
     */
    public Matrix toTriangle() {
        gaussSteps.clear();     // otherwise every call kept adding steps to the old ones
        Elimination e = eliminate(null, gaussSteps);
        detSign = e.sign();
        return e.triangle();
    }

    /**
     * Gauss elimination with partial pivoting. The ONLY copy of the algorithm
     * (used by toTriangle(), calcDeterminant() and SystemMatrix).
     * Works on a copy: this matrix is never modified.
     *
     * @param companion if not null, gets the same row operations (the b of Ax = b)
     * @param steps     if not null, each step is added here as text
     * @return the triangle, the swap sign and whether some column had no pivot
     */
    protected final Elimination eliminate(TransformableVector companion, List<String> steps) {
        Matrix triang = new Matrix(this);
        int m = triang.getNumRows();
        int n = triang.getNumCols();
        double tol = pivotTolerance();
        double sign = 1.0;
        boolean noPivot = false;
        int pivotRow = 0;

        for (int col = 0; col < n; col++) {
            if (pivotRow >= m) {
                noPivot = true;     // more columns than rows: some column has no pivot
                break;
            }

            // Partial pivoting: use the row with the largest |value| in this column
            // (avoids dividing by tiny numbers).
            int best = pivotRow;
            for (int r = pivotRow + 1; r < m; r++) {
                if (Math.abs(triang.get(r, col)) > Math.abs(triang.get(best, col))) {
                    best = r;
                }
            }

            if (Math.abs(triang.get(best, col)) <= tol) {
                // Column with no pivot: clear the rounding leftovers so they are not
                // mistaken for real values later
                for (int r = pivotRow; r < m; r++) {
                    triang.rows.get(r).set(col, 0.0);
                }
                noPivot = true;
                continue;
            }

            // Bring the pivot row up. Each swap flips the determinant's sign.
            if (best != pivotRow) {
                String step = triang.rowSwap(pivotRow, best);
                if (companion != null) companion.rowSwap(pivotRow, best);
                if (steps != null) steps.add(step);
                sign = -sign;
            }

            // Zero out every value below the pivot: R_r = R_r - factor * R_pivot.
            double pivot = triang.get(pivotRow, col);
            for (int r = pivotRow + 1; r < m; r++) {
                double factor = triang.get(r, col) / pivot;
                if (factor != 0.0) {
                    String step = triang.rowComp(r, pivotRow, -factor);
                    if (companion != null) companion.rowComp(r, pivotRow, -factor);
                    if (steps != null) steps.add(step);
                    triang.rows.get(r).set(col, 0.0);   // exact zero, avoids 1e-16 leftovers
                }
            }

            pivotRow++;
        }

        // Any value smaller than the tolerance left over in the triangle is noise
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < n; c++) {
                if (Math.abs(triang.get(r, c)) <= tol) {
                    triang.rows.get(r).set(c, 0.0);
                }
            }
        }

        triang.invalidateDeterminant();     // its values were edited directly above
        return new Elimination(triang, sign, noPivot);
    }

    /** New matrix with rows and columns swapped (n x m -> m x n). */
    public Matrix transpose() {
        List<Vector> transposed = new ArrayList<>();
        for (int c = 0; c < size[1]; c++) {
            List<Double> vals = new ArrayList<>();
            for (int r = 0; r < size[0]; r++) {
                vals.add(get(r, c));
            }
            transposed.add(new Vector(vals));   // column c becomes row c
        }
        return build(transposed);
    }

    //--Matrix Arithmetic--

    /**
     * Returns this + m.
     *
     * @throws IllegalArgumentException if sizes differ
     */
    @Override
    public Matrix add(Matrix m) {
        if (this.getNumCols() != m.getNumCols() || this.getNumRows() != m.getNumRows()) {
            throw new IllegalArgumentException("invalid sum sizes");
        }

        List<Vector> vectors1 = this.getRows();
        List<Vector> vectors2 = m.getRows();
        List<Vector> resultm = new ArrayList<Vector>();

        for (int i = 0; i < vectors1.size(); i++) {
            resultm.add(vectors1.get(i).add(vectors2.get(i)));
        }

        return Matrix.build(resultm);
    }


    /** Returns this - a, computed as this + (-1 * a). */
    @Override
    public Matrix subtract(Matrix a) {
        return this.add(a.scalarMult(-1));
    }


    /** Returns k * this. */
    @Override
    public Matrix scalarMult(double k) {
        List<Vector> vectors = this.getRows();
        List<Vector> resultm = new ArrayList<Vector>();

    for(int i = 0; i < vectors.size(); i++) {
            resultm.add(vectors.get(i).scalarMult(k));
        }

        return Matrix.build(resultm);
    }

    /**
     * Matrix product m1 * m2 ((n x k) * (k x p) = n x p).
     * Entry (i, j) = row i of m1 . column j of m2.
     *
     * @throws IllegalArgumentException if m1 columns != m2 rows
     */
    public static Matrix multiply(Matrix m1, Matrix m2) {
        if (m1.getNumCols() != m2.getNumRows()) {
            throw new IllegalArgumentException("Not valid sizes");
        }

        List<Vector> vectorsm1 = m1.getRows();
        // Transpose m2 so its columns become rows -> plain dot products.
        List<Vector> vectorsm2 = m2.transpose().getRows();
        List<Vector> resultm = new ArrayList<Vector>();
        List<Double> rowResult = new ArrayList<Double>();

        for (int i = 0; i < vectorsm1.size(); i++) {
            Vector row = vectorsm1.get(i);

            for (int j = 0; j < vectorsm2.size(); j++) {
                Vector column = vectorsm2.get(j);

                rowResult.add(row.dotProduct(column));
            }

            resultm.add( new Vector(rowResult));
            rowResult.clear();
        }

        return Matrix.build(resultm);
    }

    /** m1 * v, with v used as a column vector. Returns a Vector. */
    public static Vector multiply(Matrix m1, Vector v) {
        Matrix m2 = Matrix.convertToMatrix(v);

        return Vector.convertToVector(Matrix.multiply(m1, m2));
    }

    /**
     * m^k for whole k (square only).
     * k = 0 -> identity, negative k -> inverse(m)^|k|. Does |k| multiplications.
     *
     * @throws IllegalArgumentException if k is not whole or m is not square
     */
    public static Matrix pow(Matrix m, double k) {
        if (!(k % 1 == 0)) {
            throw new IllegalArgumentException("Matrix powers only work with integer powers!");
        }

        if (m.getNumCols() != m.getNumRows()) {
            throw new IllegalArgumentException("Matrix powers only work on square matrices!");
        }

        if (k == 0) {
            return identity(m.getNumCols());
        }

        if (k < 0) {
            return pow(inverse(m), -k);
        }

        return multiply(m, pow(m, k - 1));
    }

    //---Elemental Matrix Operations---

    /** R_idx = a * R_idx. In place; invalidates the determinant cache. */
    @Override
    public String rowMultiply(double a, int idx) {
        this.rows.set(idx, this.rows.get(idx).scalarMult(a));
        invalidateDeterminant();

        return String.format("R_%d -> R_%d * %s\n", idx, idx, Vector.formatNumber(a));
    }


    /** Swaps two rows. In place; invalidates the determinant cache. */
    @Override
    public String rowSwap(int idx1, int idx2) {
        Collections.swap(this.rows, idx1, idx2);
        invalidateDeterminant();

        return String.format("R_%d <-> R_%d\n", idx1, idx2);
    }


    /** R_idx1 = R_idx1 + k * R_idx2. In place; invalidates the determinant cache. */
    @Override
    public String rowComp(int idx1, int idx2, double k) {
        Vector target = this.rows.get(idx1);
        Vector source = this.rows.get(idx2);

        this.rows.set(idx1, target.add(source.scalarMult(k)));
        invalidateDeterminant();    // does not change det in exact math, but it can change the rounding
        return String.format("R_%d -> R_%d + %s * R_%d\n", idx1, idx1, Vector.formatNumber(k), idx2);
    }

    //--- Misc methods---

    /** Deep copy of all rows. */
    public List<Vector> getRows() {
        List<Vector> copy = new ArrayList<>();
        for (Vector row : rows) {
            copy.add(new Vector(row));
        }
        return copy;
    }

    /** Copy of one row (0-based). */
    public Vector getRow(int row) {
        Objects.checkIndex(row, size[0]);
        return new Vector(rows.get(row));
    }

    /** Copy of one column (0-based) as a Vector. */
    public Vector getColumn(int col) {
        Objects.checkIndex(col, size[1]);
        List<Double> values = new ArrayList<>();
        for (Vector row : rows) {
            values.add(row.get(col));
        }
        return new Vector(values);
    }

    /** Sets one entry (0-based). Invalidates the determinant cache. */
    public void set(int row, int col, double value) {
        Objects.checkIndex(row, size[0]);
        Objects.checkIndex(col, size[1]);
        rows.get(row).set(col, value);
        invalidateDeterminant();
    }

    /**
     * Replaces one row (copied). Invalidates the determinant cache.
     *
     * @throws IllegalArgumentException if the row has the wrong size
     */
    public void setRow(int row, Vector values) {
        Objects.checkIndex(row, size[0]);
        Objects.requireNonNull(values, "The row cannot be null.");
        if (values.getDimension() != size[1]) {
            throw new IllegalArgumentException("The row must have " + size[1]
                    + " columns, but has " + values.getDimension() + ".");
        }
        rows.set(row, new Vector(values));
        invalidateDeterminant();
    }

    /** True if rows == columns. */
    public boolean isSquare() {
        return size[0] == size[1];
    }

    /** {numRows, numCols} (copy). */
    public int[] getSize() {
        return size.clone();
    }

    /** Read-only list of the row operations of the last toTriangle()/gaussJordan(). */
    public List<String> getGaussSteps() {
        return List.copyOf(gaussSteps);
    }

    /** Same class and same rows (compared with Vector.equals). */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Matrix other = (Matrix) o;
        return rows.equals(other.rows);
    }

    /** Consistent with equals. */
    @Override
    public int hashCode() {
        return rows.hashCode();
    }


    /** n x n identity matrix. */
    public static Matrix identity(int n) {
        List<Vector> rows = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Vector row = new Vector(n);
            row.set(i, 1.0);
            rows.add(row);
        }

        return Matrix.build(rows);
    }

    /** Vector -> n x 1 column matrix. */
    public static Matrix convertToMatrix(Vector v) {
        return Matrix.build(List.of(v)).transpose();
    }

    /** Number of rows. */
    public int getNumRows() { return size[0]; }

    /** Number of columns. */
    public int getNumCols() { return size[1]; }

    /** Entry at (row, col), 0-based. */
    public double get(int row, int col) {
        Objects.checkIndex(row, size[0]);
        return rows.get(row).get(col);      // no need to copy the whole row for one value
    }

    /**
     * Determinant (computed on first call, then cached). Exactly 0 if singular.
     *
     * @throws IllegalArgumentException if not square
     */
    public double getDeterminant() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Only square matrices have a determinant, this one is "
                    + size[0] + "x" + size[1] + ".");
        }
        ensureDeterminant();
        return determinant;
    }

    /**
     * One row per line, columns right-aligned:
     * <pre>
     * [1  -2]
     * [3   4]
     * </pre>
     */
    @Override
    public String toString() {
        int numRows = size[0];
        int numCols = size[1];

        String[][] cells = new String[numRows][numCols];
        int[] widths = new int[numCols];
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                cells[r][c] = Vector.formatNumber(get(r, c));
                widths[c] = Math.max(widths[c], cells[r][c].length());
            }
        }

        StringBuilder text = new StringBuilder();
        for (int r = 0; r < numRows; r++) {
            text.append("[");
            for (int c = 0; c < numCols; c++) {
                if (c > 0) text.append("  ");
                text.append(String.format("%" + widths[c] + "s", cells[r][c]));
            }
            text.append("]");
            if (r < numRows - 1) text.append("\n");
        }
        return text.toString();
    }
}
