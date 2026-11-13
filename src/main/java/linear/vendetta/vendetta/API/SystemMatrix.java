/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.API;

/**
 *
 * @author ian
 */
public class SystemMatrix extends Matrix{
    private TransformableVector eqVector;
    
    public SystemMatrix(Matrix m, Vector v) {
        super(m);

        if (v == null || v.getDimension() != m.getNumRows()) {
            throw new IllegalArgumentException("The vector must have one value per row of the matrix ("
                    + m.getNumRows() + ").");
        }

        eqVector = new TransformableVector(v);
    }
   
    
    public Vector gaussJordan() {
        return null;
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
