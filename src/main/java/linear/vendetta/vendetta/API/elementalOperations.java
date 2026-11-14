package linear.vendetta.vendetta.API;

/**
 * The 3 elementary row operations (used by Gaussian elimination).
 * Each one changes the object IN PLACE and returns the step as text,
 * e.g. {@code "R_0 <-> R_2"}, so it can be shown to the user.
 *
 * @author Alex
 */
public interface elementalOperations<T> {
    /** R_idx = a * R_idx */
    String rowMultiply(double a, int idx);
    /** {@code R_idx1 <-> R_idx2} */
    String rowSwap(int idx1, int idx2);
    /** R_idx1 = R_idx1 + k * R_idx2 */
    String rowComp(int idx1, int idx2, double k);
}
