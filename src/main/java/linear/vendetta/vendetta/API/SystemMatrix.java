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
    
    /*
    public Vector gaussJordan() {
        
    }
    */

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
