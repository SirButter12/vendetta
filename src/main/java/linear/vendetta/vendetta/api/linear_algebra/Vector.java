package linear.vendetta.vendetta.api.linear_algebra;

import linear.vendetta.vendetta.api.Helpers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * Vector of doubles with at least 1 component.
 * Operations return NEW vectors; only set() and setValues() modify this one.
 *
 * @author Alex, Ian
 */
public class Vector implements Operable<Vector> {


    /** The components of the vector in order: x, y, z, ... */
    protected List<Double> values;
    // length and direction are NOT stored: they are computed on demand in
    // getLen() / getDirection(), so they can never go stale and the zero
    // vector (used everywhere internally) can still be built.

    /** How many components the vector has. Always equals values.size(). */
    private int dimension;



    /**
     * Vector from a list of numbers (any Number type, converted to double).
     * The list is copied.
     *
     * @throws IllegalArgumentException if the list is null, empty or has a null
     */
    public <T extends Number> Vector(List<T> values) {
        // Convert each Number to Double (a plain cast would break with List.of(1, 2))
        if (values == null) {
            throw new IllegalArgumentException("A vector needs at least one component.");
        }
        List<Double> doubles = new ArrayList<>(values.size());
        for (T component : values) {
            doubles.add(component == null ? null : component.doubleValue());
        }
        this.values = validatedCopy(doubles);
        this.dimension = this.values.size();
    }

    /** Copy constructor. */
    public Vector(Vector v) {
        this.values = new ArrayList<>(v.values);
        this.dimension = v.dimension;
    }

    /**
     * Zero vector of the given size.
     *
     * @throws IllegalArgumentException if dimension is less than 1
     */
    public Vector(int dimension) {
        if (dimension < 1) {
            throw new IllegalArgumentException(
                    "A vector needs a dimension of at least 1, but got " + dimension + ".");
        }
        this.values = new ArrayList<>(Collections.nCopies(dimension, 0.0));
        this.dimension = dimension;
    }


    /** Number of components. */
    public int getDimension() {
        return dimension;
    }


    /** Read-only copy of the components. */
    public List<Double> getValues() {
        return List.copyOf(values);
    }

    /** Replaces all components (list is copied, size may change). */
    public void setValues(List<Double> newValues) {
        this.values = validatedCopy(newValues);
        this.dimension = this.values.size();
    }

    /** Component at index (0-based). */
    public double get(int index) {
        Objects.checkIndex(index, dimension);
        return values.get(index);
    }
    
    /** Length (Euclidean norm). Computed from the current components. */
    public double getLen() {
        return calcLen();
    }

    /**
     * Direction angles in radians (polar / hyperspherical form), n-1 angles
     * for n components. See directionAngles().
     *
     * @throws ArithmeticException for the zero vector
     */
    public List<Double> getDirection() {
        return directionAngles();
    }

    /** Sets the component at index (0-based). In place. */
    public void set(int index, double value) {
        Objects.checkIndex(index, dimension);
        values.set(index, value);
    }


    /** Returns k * this. */
    @Override
    public Vector scalarMult(double n) {
        Vector v = new Vector(this);

        for (int i = 0; i < dimension; i++) {
            v.values.set(i, values.get(i) * n);
        }

        return v;
    }

    /**
     * Returns this + a.
     *
     * @throws IllegalArgumentException if dimensions differ
     */
    @Override
    public Vector add(Vector a) {
        validateDimensions(this, a);

        Vector sum = new Vector(this);

        for (int i = 0; i < dimension; i++) {
            sum.values.set(i, sum.values.get(i) + a.values.get(i));
        }

        return sum;
    }

    /**
     * Returns this - a.
     *
     * @throws IllegalArgumentException if dimensions differ
     */
    @Override
    public Vector subtract(Vector a) {
        return this.add(a.scalarMult(-1));
    }

    /**
     * Dot product: sum of this[i] * a[i].
     *
     * @throws IllegalArgumentException if dimensions differ
     */
    public double dotProduct(Vector a) {
        validateDimensions(this, a);
        double sum = 0.0;
        for (int i = 0; i < dimension; i++) {
            sum += values.get(i) * a.values.get(i);
        }
        return sum;
    }

    /**
     * 2D "cross product": a1*b2 - a2*b1 (signed area, the z of the 3D cross).
     *
     * @throws IllegalArgumentException if either vector is not 2D
     */
    public double cross2D(Vector v) {
        requireBothDimension(v, 2);
        return values.get(0) * v.values.get(1) - values.get(1) * v.values.get(0);
    }


    /**
     * 3D cross product: vector perpendicular to both.
     *
     * @throws IllegalArgumentException if either vector is not 3D
     */
    public Vector cross3D(Vector v) {
        requireBothDimension(v, 3);

        double a1 = values.get(0), a2 = values.get(1), a3 = values.get(2);
        double b1 = v.values.get(0), b2 = v.values.get(1), b3 = v.values.get(2);

        List<Double> result = List.of(
                a2 * b3 - a3 * b2,
                a3 * b1 - a1 * b3,
                a1 * b2 - a2 * b1);
        return new Vector(result);
    }

    /**
     * Angle between two vectors, in radians, from 0 to pi:
     * acos( (v . u) / (|v| * |u|) ).
     * The vectors can have different dimensions: the smaller one is padded with
     * zeros up to the dimension of the bigger one, e.g. [1, 2] and [1, 2, 3]
     * are compared as [1, 2, 0] and [1, 2, 3].
     *
     * @throws ArithmeticException if either vector is the zero vector (it has no direction)
     */
    public static double angleInBet(Vector v, Vector u) {
    
        int dimension = Math.max(v.dimension, u.dimension);
        v = v.padTo(dimension);
        u = u.padTo(dimension);

        double vLen = v.getLen();
        double uLen = u.getLen();
        if (vLen == 0.0 || uLen == 0.0) {
            throw new ArithmeticException("The angle with the zero vector is undefined.");
        }

        Vector a = v.scalarMult(uLen);
        Vector b = u.scalarMult(vLen);
        return 2 * Math.atan2(a.subtract(b).getLen(), a.add(b).getLen());
    }

    /**
     * Copy of this vector with zeros added at the end up to the given dimension,
     * e.g. [1, 2].padTo(3) = [1, 2, 0]. This vector is not modified.
     *
     * @throws IllegalArgumentException if dimension is smaller than this vector's dimension
     */
    public Vector padTo(int dimension) {
        if (dimension < this.dimension) {
            throw new IllegalArgumentException("Cannot pad a vector of dimension " + this.dimension
                    + " down to dimension " + dimension + ".");
        }

        Vector padded = new Vector(this);
        while (padded.values.size() < dimension) {
            padded.values.add(0.0);
        }
        padded.dimension = dimension;
        return padded;
    }

    /**
     * Projection of this onto v: (this.v / v.v) * v.
     *
     * @throws ArithmeticException if v is the zero vector
     */
    public Vector projectOn(Vector v) {
        validateDimensions(this, v);

        double vDotV = v.dotProduct(v);
        if (vDotV == 0.0) {
            throw new ArithmeticException("Cannot project onto the zero vector.");
        }

        double factor = this.dotProduct(v) / vDotV;

        return v.scalarMult(factor);
    }
    
    /**
     * Longitud (norma euclidiana) del vector: sqrt(x1² + x2² + ... + xn²).
     *
     * @return la longitud, siempre >= 0
     */
    private double calcLen() {
        double len = 0;

        for (double value: values) {
            len += value * value;
        }

        return Math.sqrt(len);
    }

    /**
     * Hyperspherical angles (radians). 2D: [theta] with theta = atan2(y, x).
     * nD: phi_k = atan2(|(x_{k+1}, ..., x_n)|, x_k) for k < n-1,
     * last one = atan2(x_n, x_{n-1}). 1D: [0] if positive, [pi] if negative.
     *
     * @throws ArithmeticException for the zero vector
     */
    private List<Double> directionAngles() {
        if (calcLen() == 0.0) {
            throw new ArithmeticException("The zero vector has no direction.");
        }

        int n = dimension;
        if (n < 2) {
            return List.of(values.get(0) < 0 ? Math.PI : 0.0);
        }

        Double[] angles = new Double[n - 1];
        angles[n - 2] = Math.atan2(values.get(n - 1), values.get(n - 2));

        double tail = Math.hypot(values.get(n - 1), values.get(n - 2));
        for (int k = n - 3; k >= 0; k--) {
            angles[k] = Math.atan2(tail, values.get(k));
            tail = Math.hypot(tail, values.get(k));
        }

        return List.of(angles);
    }
    
        /**
     * Builds a vector from its polar (hyperspherical) form. Inverse of getDirection():
     * x1 = r cos t1, x2 = r sin t1 cos t2, ..., xn = r sin t1 ... sin t(n-1).
     * No angles -> 1D vector [r] (r may be negative).
     *
     * @throws IllegalArgumentException if r < 0 and there are angles
     */
    public static Vector fromPolar(double r, List<Double> angles) {
        if (r < 0 && !angles.isEmpty()) {
            throw new IllegalArgumentException("The length cannot be negative.");
        }
        List<Double> comps = new ArrayList<>(angles.size() + 1);
        double sinProduct = r;
        for (double angle : angles) {
            comps.add(snap(sinProduct * Math.cos(angle), r));
            sinProduct *= Math.sin(angle);
        }
        comps.add(snap(sinProduct, r));
        return new Vector(comps);
    }

    /** Polar form of this vector. 1D: no angles, signed length. */
    public PolarForm toPolar() {
        if (dimension == 1) {
            return new PolarForm(values.get(0), List.of());
        }
        return new PolarForm(getLen(), getDirection());
    }

    /** cos(pi/2) gives 6e-17 instead of 0: treat tiny values (relative to r) as 0. */
    private static double snap(double x, double r) {
        return Math.abs(x) < 1e-12 * Math.max(1.0, Math.abs(r)) ? 0.0 : x;
    }

    /**
     * n x 1 (column) matrix -> Vector.
     *
     * @throws IllegalArgumentException if m has more than 1 column
     */
    public static Vector convertToVector(Matrix m) {
        if (m.getNumCols() != 1) {
            throw new IllegalArgumentException("m is not a vector matrix");
        }

        return m.transpose().getRows().get(0);
    }


    /**
     * Same dimension and same components (exact compare, -0 == 0).
     * Uses instanceof, so a TransformableVector can equal a Vector.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vector other)) return false;
        if (this.dimension != other.dimension) return false;

        for (int i = 0; i < dimension; i++) {
            if (Double.compare(clean(values.get(i)), clean(other.values.get(i))) != 0) {
                return false;
            }
        }
        return true;
    }

    /** Consistent with equals (also treats -0 as 0). */
    @Override
    public int hashCode() {
        int result = 1;
        for (double x : values) {
            result = 31 * result + Double.hashCode(clean(x));
        }
        return result;
    }

    /** Display text, e.g. [1, 2.5, -3]. Numbers go through helpers.formatNumber(). */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < dimension; i++) {
            if (i > 0) text.append(", ");
            text.append(Helpers.formatNumber(values.get(i)));
        }
        return text.append("]").toString();
    }

    /**
     * Guard for two-vector operations (add, subtract, dotProduct, projectOn).
     *
     * @throws IllegalArgumentException if sizes differ
     * @throws NullPointerException if v is null
     */
    private static void validateDimensions(Vector v, Vector u) {
        Objects.requireNonNull(v, "The other vector cannot be null.");
        if (v.dimension != u.dimension) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimension, but got "
                            + v.dimension + " and " + u.dimension + ".");
        }
    }
    /**
     * Guard for cross products: both vectors must have exactly `required` components.
     *
     * @throws IllegalArgumentException otherwise
     */
    private void requireBothDimension(Vector v, int required) {
        Objects.requireNonNull(v, "The other vector cannot be null.");
        if (this.dimension != required || v.dimension != required) {
            throw new IllegalArgumentException(
                    "This operation needs two " + required + "-dimensional vectors, but got "
                            + this.dimension + " and " + v.dimension + ".");
        }
    }

    /**
     * Copies the list, rejecting null/empty lists and null components.
     *
     * @throws IllegalArgumentException if invalid
     */
    private static List<Double> validatedCopy(List<Double> source) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("A vector needs at least one component.");
        }
        List<Double> copy = new ArrayList<>(source.size());
        for (Double component : source) {
            if (component == null) {
                throw new IllegalArgumentException("A vector cannot contain null components.");
            }
            copy.add(component);
        }
        return copy;
    }


    /**
     * Turns -0.0 into 0.0 (adding 0.0 does exactly that).
     * Without this, [0] and [-0] (which appears after scalarMult(-1) on a zero)
     * would count as different vectors.
     */
    private static double clean(double x) {
        return x + 0.0;
    }
}