/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.API;

import java.util.Collections;

/**
 *
 * @author ian
 */
public class TransformableVector extends Vector implements elementalOperations {

    public TransformableVector(Vector v) {
        super(v);
    }
    
    @Override
    public String rowMultiply(double a, int idx) {
        super.set(idx, this.values.get(idx) * a);  
        return String.format("R_%d -> R_%d * %s\n", idx, idx, Vector.formatNumber(a));
    }

    @Override
    public String rowSwap(int idx1, int idx2) {
        Collections.swap(this.values, idx1, idx2);
        return String.format("R_%d <-> R_%d\n", idx1, idx2);
    }

    @Override
    public String rowComp(int idx1, int idx2, double k) {
        super.set(idx1, this.values.get(idx1) + this.values.get(idx2) * k);
        return String.format("R_%d -> R_%d + %s * R_%d\n", idx1, idx1, Vector.formatNumber(k) , idx2);
    }
}
