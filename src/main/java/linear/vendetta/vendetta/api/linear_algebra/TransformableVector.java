package linear.vendetta.vendetta.api.linear_algebra;

import linear.vendetta.vendetta.api.helpers;
import java.util.Collections;

/**
 * Vector that supports row operations, treating each component as a "row".
 * Used as the right-hand side b of Ax = b, so b gets the same row
 * operations as the matrix A during elimination.
 *
 * @author ian
 */
public class TransformableVector extends Vector implements elementalOperations {

    /** Copy of v that can be transformed. */
    public TransformableVector(Vector v) {
        super(v);
    }

    /** b[idx] = a * b[idx]. In place, returns the step text. */
    @Override
    public String rowMultiply(double a, int idx) {
        super.set(idx, this.values.get(idx) * a);
        return helpers.rowMultiplyStep(a, idx);
    }

    /** Swaps b[idx1] and b[idx2]. In place, returns the step text. */
    @Override
    public String rowSwap(int idx1, int idx2) {
        Collections.swap(this.values, idx1, idx2);
        return helpers.rowSwapStep(idx1, idx2);
    }

    /** b[idx1] = b[idx1] + k * b[idx2]. In place, returns the step text. */
    @Override
    public String rowComp(int idx1, int idx2, double k) {
        super.set(idx1, this.values.get(idx1) + this.values.get(idx2) * k);
        return helpers.rowCompStep(idx1, idx2, k);
    }
}
