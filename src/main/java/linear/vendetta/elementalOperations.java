/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package linear.vendetta;

/**
 *
 * @author Alex
 */
public interface elementalOperations<o> {
    o rowMultiply(double a, int idx);
    o rowSwap(int idx1, int idx2);
    o rowComp(int idx1, int idx2, double k);
}
