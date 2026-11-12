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
    private List<Vector> rows;
    private int[] size = new int[2];
    private double determinant;
    private double detSign;


       
     // Private: go through build()
    private Matrix(List<Vector> rows) {
        if (rows == null || rows.isEmpty()) {
            throw new IllegalArgumentException("A matrix needs at least 1 row.");
        }
        this.rows = rows;
        this.size[0] = rows.size();
        this.size[1] = vectorValues(rows.get(0)).size();
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
        this.rows = m.rows;
        this.size = m.size;
        this.determinant = m.determinant;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 
    

    
    
    
    public Matrix getSubMatrix(int i , int j) {
        List<Vector> equations = new ArrayList<>();
        
        return build(equations);
    }
    
    public static Matrix adj(Matrix m) {
        return m;
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
    
    public List<Vector> getRows() {
        return List.copyOf(rows);
    }    
    
    /**
     * Matrix builder. Each Vector is one row; the size of its
     * List is the number of columns.
     */
    public static Matrix build(List<Vector> rows) {
        return new Matrix(new ArrayList<>(rows)); // defensive copy of the list
    }
 
    // Single point of contact with your Vector class.
    private static List<Double> vectorValues(Vector v) {
        return v.getValues(); // <-- rename if your getter differs
    }
 
    // Getters
  
    public int getNumRows() { return size[0]; }
 
    public int getNumCols() { return size[1]; }
 
    public double get(int row, int col) { return vectorValues(rows.get(row)).get(col); }
    
    
    public static Matrix inverse(Matrix m) {
        Matrix adj = Matrix.adj(m);
        
        return adj.scalarMult(1 / m.getDeterminant());
    }
    
    @Override  
    public void rowMultiply(double a, int idx) {
        Vector oldVector = this.rows.get(idx);
        Vector newVector = oldVector.scalarMult(a); 
        this.rows.set(idx, newVector);
        this.determinant = determinant * a;
    }


    @Override
    public void rowSwap(int idx1, int idx2) {
        Collections.swap(this.rows, idx1, idx2); //skeptical if it works but it
        //should work
        
        this.determinant = determinant * -1;
    }


    @Override
    public void rowComp(int idx1, int idx2, double k) {
        List<Double> target = vectorValues(this.rows.get(idx1));
        List<Double> source = vectorValues(this.rows.get(idx2));
        
        List<Double> combined = new ArrayList<>();
        for (int c = 0; c < size[1]; c++) {
            combined.add(target.get(c) + k * source.get(c));
        }
        
        this.rows.set(idx1, new Vector(combined));
    }
    
    public void calcDeterminant() {
        if (size[0] != size[1]) {
            determinant = NaN;
        }
        Matrix tri = toTriangle();          
        double total = 1.0;
        for (int i = 0; i < size[0]; i++) {
            total *= tri.get(i, i);         
        }
        determinant = total * detSign;
    }


    public double getDeterminant() {
        calcDeterminant();
        return determinant;
    }
    
    public Matrix toTriangle() { //gaussian elim (had to remove static to read
        //the rows
         int m = size[0];
    int n = size[1];
    double[][] a = new double[m][n];
    for (int r = 0; r < m; r++) {
        for (int c = 0; c < n; c++) {
            a[r][c] = get(r, c);
        }
    }


    // Determinant tracker: each row swap multiplies the determinant by -1.
    detSign = 1.0;


    int pivotRow = 0;
    for (int col = 0; col < n && pivotRow < m; col++) {


        int best = pivotRow;
        for (int r = pivotRow + 1; r < m; r++) {
            if (Math.abs(a[r][col]) > Math.abs(a[best][col])) {
                best = r;
            }
        }


        // No usable pivot if that small, move onto next pivot
        if (Math.abs(a[best][col]) < 0.00001) {
            continue;
        }


        // Swap the best row into the pivot position (only counts if rows differ).
        if (best != pivotRow) {
            double[] temp = a[pivotRow];
            a[pivotRow] = a[best];
            a[best] = temp;
            detSign *= -1.0;
        }


        for (int r = pivotRow + 1; r < m; r++) {
            double factor = a[r][col] / a[pivotRow][col];
            a[r][col] = 0.0;
            for (int c = col + 1; c < n; c++) {
                a[r][c] -= factor * a[pivotRow][c];
            }
        }
        pivotRow++;
    }


    return fromArray(a);
}
    
    // Helper: turns a double[][] back into a Matrix using the builder.
    //I really just added it because I'll probably need it later -Matthew
    private static Matrix fromArray(double[][] a) {
        List<Vector> result = new ArrayList<>();
        for (double[] rowData : a) {
            List<Double> vals = new ArrayList<>();
            for (double v : rowData) {
                vals.add(v);
            }
            result.add(new Vector(vals)); // assumes a Vector(List<Double>) constructor
        }
        return build(result);
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
        
    /*
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
    */
}