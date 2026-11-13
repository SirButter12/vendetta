/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.API;

import java.util.ArrayList;

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
        return Matrix.multiply(Matrix.inverse(Matrix.build(this.rows)), eqVector);
    } 
    
    /*
    public Vector gaussJordan() {
        
    }
    */
}
