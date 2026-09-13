/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta;

import java.util.List;

/**
 *
 * @author Matthew, Ian, Adrian
 */
public class Matrix {
    private List<Vector> rows;
    private int[] size = new int[2];
    private double determinant;

    
    //TO DO: REVIEW CONSTRUCTOR BASE ON NEEDS
    public Matrix(List<Vector> rows, double determinant) {
        this.rows = rows;
        this.determinant = determinant;
    }
    
    public void transpose() {
        //TO DO: CONTENT
    }
    
    public Matrix getInverse(Matrix m) {
        Vector vector = new Vector();
        this.rows.add(vector);  //TODO: Content
        return new Matrix(this.rows,404); //404 recognizable for not existing
    }
    
    public double CalcDeterminant() {
        return 404; //TODO: COntent
    }
    
    public Matrix toTriangle() {
        return new Matrix(this.rows, 404); //TODO: Content
    }
}
