package linear.vendetta.vendetta.API;

/**
 * Basic arithmetic shared by Vector and Matrix.
 * Every method returns a NEW object; the originals are not changed.
 *
 * @author Alex
 */
public interface operable<T>{
    /** Element-wise this + a. */
    T add(T a);
    /** Element-wise this - a. */
    T subtract(T a);
    /** Every element times k. */
    T scalarMult(double k);
}
