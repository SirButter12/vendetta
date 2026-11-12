/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package linear.vendetta.vendetta.API;

/**
 *
 * @author Alex
 */
public interface elementalOperations<T> {
    String rowMultiply(double a, int idx);
    String rowSwap(int idx1, int idx2);
    String rowComp(int idx1, int idx2, double k);
}
