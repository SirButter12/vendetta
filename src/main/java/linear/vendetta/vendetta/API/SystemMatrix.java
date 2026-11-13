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
        
        eqVector = new TransformableVector(v);
    }
    
    public Vector solve() {
        if (this.determinant == 0) {
            throw new MultipleOrNoSolutionException();
        }
        
        
        return multiply(inverse(this), eqVector);
    } 
    
    /*
    public Vector gaussJordan() {
        
    }
    */
}
