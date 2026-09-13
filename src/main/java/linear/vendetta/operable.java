/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package linear.vendetta;

/**
 *
 * @author Alex
 */
public interface operable<T> {
    
    T add(T a);
    T subtract(T a);
    T dotProduct(T a);
    T scalarMult(double k);
    
    //TO review: Usage of T (Placeholder)
}
