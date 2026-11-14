package linear.vendetta.vendetta.API;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


/**
 * Vector of doubles with at least 1 component.
 * Operations return NEW vectors; only set() and setValues() modify this one.
 *
 * @author Alex
 */
public class Vector implements operable<Vector> {
    /** Max decimals shown by toString() / formatNumber(). Display only. */
    private static final int DISPLAY_DECIMALS = 4;


    /** The components of the vector in order: x, y, z, ... */
    protected List<Double> values;

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
        validateDimensions(a);

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
        validateDimensions(a);
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
     * Projection of this onto v: (this.v / v.v) * v.
     *
     * @throws ArithmeticException if v is the zero vector
     */
    public Vector projectOn(Vector v) {
        validateDimensions(v);

        double vDotV = v.dotProduct(v);
        if (vDotV == 0.0) {
            throw new ArithmeticException("Cannot project onto the zero vector.");
        }

        double factor = this.dotProduct(v) / vDotV;

        return v.scalarMult(factor);
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

    /** Display text, e.g. [1, 2.5, -3]. Numbers go through formatNumber(). */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder("[");
        for (int i = 0; i < dimension; i++) {
            if (i > 0) text.append(", ");
            text.append(formatNumber(values.get(i)));
        }
        return text.append("]").toString();
    }

    /**
     * Guard for two-vector operations (add, subtract, dotProduct, projectOn).
     *
     * @throws IllegalArgumentException if sizes differ
     * @throws NullPointerException if v is null
     */
    private void validateDimensions(Vector v) {
        Objects.requireNonNull(v, "The other vector cannot be null.");
        if (this.dimension != v.dimension) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimension, but got "
                            + this.dimension + " and " + v.dimension + ".");
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


    /**
     * Number -> short display text: up to DISPLAY_DECIMALS decimals,
     * no trailing zeros, always '.' as decimal point, -0 shown as 0.
     * e.g. 2.0 -> "2", 1/3.0 -> "0.3333". NaN/Infinity printed as is.
     */
    public static String formatNumber(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            return String.valueOf(x);
        }
        // Locale.ROOT forces a "." decimal point even on a French (comma) computer.
        String text = String.format(Locale.ROOT, "%." + DISPLAY_DECIMALS + "f", x);
        if (text.contains(".")) {
            text = text.replaceAll("0+$", "").replaceAll("\\.$", "");   // trim trailing zeros
        }
        return text.equals("-0") ? "0" : text;
    }
}