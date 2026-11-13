/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.API;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ian
 */
public class SystemMatrix extends Matrix{
    private TransformableVector eqVector;
    private static final double EPS = 0.00001;
    
    public SystemMatrix(Matrix m, Vector v) {
        super(m);

        if (v == null || v.getDimension() != m.getNumRows()) {
            throw new IllegalArgumentException("The vector must have one value per row of the matrix ("
                    + m.getNumRows() + ").");
        }

        eqVector = new TransformableVector(v);
    }
   
    public Vector gaussJordan() {
        int m = getNumRows();
        int n = getNumCols();

        TransformableVector original = new TransformableVector(eqVector);
        Matrix triang;
        TransformableVector b;
        try {
            triang = toTriangle();
            b = eqVector;
        } finally {
            eqVector = original;
        }

        for (int r = 0; r < m; r++) {
            if (isZeroRow(triang, r) && Math.abs(b.get(r)) > EPS) {
                throw new MultipleOrNoSolutionException("The system has no solution (row " + r
                        + " reads 0 = " + Vector.formatNumber(b.get(r)) + ").");
            }
        }

        if (m < n) {
            throw new MultipleOrNoSolutionException("The system has infinitely many solutions ("
                    + n + " unknowns but only " + m + " equations).");
        }
        for (int i = 0; i < n; i++) {
            if (Math.abs(triang.get(i, i)) < EPS) {
                throw new MultipleOrNoSolutionException(
                        "The system has infinitely many solutions (x_" + i + " is free).");
            }
        }

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

        List<Double> solution = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            solution.add(b.get(i));
        }
        return new Vector(solution);
    }

    private static boolean isZeroRow(Matrix mat, int row) {
        for (int c = 0; c < mat.getNumCols(); c++) {
            if (Math.abs(mat.get(row, c)) > EPS) {
                return false;
            }
        }
        return true;
    }
    
    
    @Override
    public Matrix toTriangle() {
        Matrix triang = new Matrix(this);
        int m = triang.getNumRows();
        int n = triang.getNumCols();

        gaussSteps.clear();     // otherwise every call kept adding steps to the old ones
        detSign = 1.0;
        int pivotRow = 0;

        for (int col = 0; col < n && pivotRow < m; col++) {
            int best = pivotRow;
            for (int r = pivotRow + 1; r < m; r++) {
                if (Math.abs(triang.get(r, col)) > Math.abs(triang.get(best, col))) {
                    best = r;
                }
            }

            if (Math.abs(triang.get(best, col)) < 0.00001) {
                continue;
            }

            if (best != pivotRow) {
                eqVector.rowSwap(pivotRow, best);
                gaussSteps.add(triang.rowSwap(pivotRow, best));
                detSign *= -1.0;
            }

            double pivot = triang.get(pivotRow, col);
            for (int r = pivotRow + 1; r < m; r++) {
                double factor = triang.get(r, col) / pivot;
                if (factor != 0.0) {
                    eqVector.rowComp(r, pivotRow, -factor);
                    gaussSteps.add(triang.rowComp(r, pivotRow, -factor));
                    triang.rows.get(r).set(col, 0.0);   // exact zero, avoids 1e-16 leftovers
                }
            }

            pivotRow++;
        }

        return triang;
    }
   

    public Vector solve() {
        if (!isSquare()) {
            throw new IllegalArgumentException("Only square systems can be solved with the inverse.");
        }

        if (getDeterminant() == 0) {
            throw new MultipleOrNoSolutionException();
        }


        return multiply(inverse(this), eqVector);
    }

    public Vector getEqVector() {
        return new Vector(eqVector);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;    
        return eqVector.equals(((SystemMatrix) o).eqVector);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + eqVector.hashCode();
    }
    
}
