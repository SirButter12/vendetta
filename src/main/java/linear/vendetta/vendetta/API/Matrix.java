package linear.vendetta.vendetta.API;

import static java.lang.Double.NaN;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Matthew, Ian, Adrian
 */
public class Matrix implements operable<Matrix>, elementalOperations {
    protected List<Vector> rows;
    protected int[] size = new int[2];
    protected double determinant;
    protected double detSign;
    protected List<String> gaussSteps = new ArrayList<>();

    
    
    //---CONSTRUCTOR METHODS AND DETERMINANT---
    
    
     // Private: go through build()
    private Matrix(List<Vector> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("A matrix needs at least 1 row.");
        }
        this.rows = rows;
        this.size[0] = rows.size();
        this.size[1] = rows.get(0).getDimension();
        calcDeterminant();
    }


    //Ian's stuff - pushed by Matthew
    public Matrix(int n, int m) {
        if (n <= 0 || m <= 0) {
            throw new IllegalArgumentException("A matrix needs at least 1 row and 1 column");
        }

        this.rows = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            this.rows.add(new Vector(m));        
        }

        this.size = new int[]{n, m};
        calcDeterminant();
    }
    
    public Matrix(Matrix m) {
        this.rows = new ArrayList<>();
        for (Vector r : m.rows) this.rows.add(new Vector(r));
        this.size = m.size.clone();
        this.determinant = m.determinant;
    }
    
    /**
     * Matrix builder. Each Vector is one row; the size of its
     * List is the number of columns.
     */
    public static Matrix build(List<Vector> rows) {
        return new Matrix(new ArrayList<>(rows)); // defensive copy of the list
    }
    
    public void calcDeterminant() {
        if (size[0] != size[1]) {
            determinant = NaN;
            return;
        }
        
        Matrix tri = toTriangle();          
        double total = 1.0;
        for (int i = 0; i < size[0]; i++) {
            total *= tri.get(i, i);         
        }
        determinant = total * detSign;
    }
    
    
    //---MATRIX FUNCTIONS METHODS---
    
    public static Matrix inverse(Matrix m) {
        if (m.getNumRows() != m.getNumCols()) {
            throw new IllegalArgumentException("Only square matrices have an inverse.");
        }

        double det = m.getDeterminant();
        if (m.getDeterminant() == 0) {
            throw new ArithmeticException("The matrix is singular (det = 0), so it has no inverse.");
        }

        return Matrix.adj(m).scalarMult(1.0 / det);
    }

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

                result.rows.get(j).set(i, sign * minor);
            }
        }

        return result;
    }
    
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
    
    public Matrix toTriangle() {
        Matrix triang = new Matrix(this);
        int m = triang.getNumRows();
        int n = triang.getNumCols();

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
                gaussSteps.add(triang.rowSwap(pivotRow, best));
                detSign *= -1.0;
            }

            double pivot = triang.get(pivotRow, col);
            for (int r = pivotRow + 1; r < m; r++) {
                double factor = triang.get(r, col) / pivot;
                if (factor != 0.0) {
                    gaussSteps.add(triang.rowComp(r, pivotRow, -factor));
                    triang.rows.get(r).set(col, 0.0);   // exact zero, avoids 1e-16 leftovers
                }
            }

            pivotRow++;
        }

        return triang;
    }
    
     /**
     * Hey, @Matthew here, um see, I had a stroke of inspiration here and I am 
     * semi-confident in how functional this code is. I actually drew up my 
     * thoughts on paper for this one. I am too tired to test it out.
     * @return the matrix that is now transposed
     */
    public Matrix transpose() {
        List<Vector> transposed = new ArrayList<>();
        for (int c = 0; c < size[1]; c++) {
            List<Double> vals = new ArrayList<>();
            for (int r = 0; r < size[0]; r++) {
                vals.add(get(r, c));
            }
            transposed.add(new Vector(vals)); // assumes a Vector(List<Double>) constructor
        }
        return build(transposed);
    }

    //--Matrix Arithmetic--
    
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


    @Override
    public Matrix subtract(Matrix a) {
        return this.add(a.scalarMult(-1)); 
    }


    @Override
    public Matrix scalarMult(double k) {
        List<Vector> vectors = this.getRows();
        List<Vector> resultm = new ArrayList<Vector>();
        
    for(int i = 0; i < vectors.size(); i++) {
            resultm.add(vectors.get(i).scalarMult(k));
        }
        
        return Matrix.build(resultm);
    }
    
    public static Matrix multiply(Matrix m1, Matrix m2) {
        if (m1.getNumCols() != m2.getNumRows()) {
            throw new IllegalArgumentException("Not valid sizes");
        }

        List<Vector> vectorsm1 = m1.getRows();
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
    
    public static Vector multiply(Matrix m1, Vector v) {
        Matrix m2 = Matrix.convertToMatrix(v);
        
        return Vector.convertToVector(Matrix.multiply(m1, m2));
    }
    
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
    
    @Override  
    public String rowMultiply(double a, int idx) { 
        this.rows.set(idx, this.rows.get(idx).scalarMult(a));
        this.determinant = determinant * a;
        
        return String.format("R_%d -> R_%d * %s\n", idx, idx, Vector.formatNumber(a));
    }


    @Override
    public String rowSwap(int idx1, int idx2) {
        Collections.swap(this.rows, idx1, idx2); //skeptical if it works but it
        //should work
        
        this.determinant = determinant * -1;
        
        return String.format("R_%d <-> R_%d\n", idx1, idx2);
    }


    @Override
    public String rowComp(int idx1, int idx2, double k) {
        Vector target = this.rows.get(idx1);
        Vector source = this.rows.get(idx2);
        
        this.rows.set(idx1, target.add(source.scalarMult(k)));
        return String.format("R_%d -> R_%d + %s * R_%d\n", idx1, idx1, Vector.formatNumber(k), idx2);
    }
    
    //--- Misc methods---
    
    public List<Vector> getRows() {
        return List.copyOf(rows);
    }    
    
    
    public static Matrix identity(int n) {
        List<Vector> rows = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Vector row = new Vector(n);  
            row.set(i, 1.0);             
            rows.add(row);
        }

        return Matrix.build(rows);
    }
    
    public static Matrix convertToMatrix(Vector v) {
        return Matrix.build(List.of(v)).transpose();
    }
  
    public int getNumRows() { return size[0]; }
 
    public int getNumCols() { return size[1]; }
 
    public double get(int row, int col) { return rows.get(row).getValues().get(col); }
    
    public double getDeterminant() {
        return determinant;
    }
        
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