/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 *
 * @author Alex
 */
public abstract class Vector implements operable<Vector> {

    /** The components of the vector in order: x, y, z, ... */
    private List<Double> values;
 
    /** How many components the vector has. Always equals values.size(). */
    private int dimension;
 
 
    
    /**
     * Builds a vector from a known list of components.
     * The list is COPIED, so changing the original list later will not
     * secretly change this vector.
     *
     * @throws IllegalArgumentException if the list is null, empty,
     * or contains a null entry
     */
    public Vector(List<Double> values) {
        this.values = validatedCopy(values);
        this.dimension = this.values.size();
    }
 
    /**
     * Builds a vector of the given size where every component is 0.0.
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

    public int getDimension() {
        return dimension;
    }

    public List<Double> getValues() {
        return List.copyOf(values);
    }
 
    public void setValues(List<Double> newValues) {
        this.dimension = this.values.size();
        this.values = validatedCopy(newValues);
    }
 
    /** Gets one component. Index starts at 0. */
    public double get(int index) {
        Objects.checkIndex(index, dimension);
        return values.get(index);
    }
 
    public void set(int index, double value) {
        Objects.checkIndex(index, dimension);
        values.set(index, value);
    }
 
    
    @Override
    public void scalarMult(double n) {
        for (int i = 0; i < dimension; i++) {
            values.set(i, values.get(i) * n);
        }
    }
 
    @Override
    public void add(Vector a) {
        validateDimensions(a);
        for (int i = 0; i < dimension; i++) {
            values.set(i, values.get(i) + a.values.get(i));
        }
    }
 
    @Override
    public void subtract(Vector a) {
        validateDimensions(a);
        for (int i = 0; i < dimension; i++) {
            values.set(i, values.get(i) - a.values.get(i));
        }
    }
 
    public double dotProduct(Vector a) {
        validateDimensions(a);
        double sum = 0.0;
        for (int i = 0; i < dimension; i++) {
            sum += values.get(i) * a.values.get(i);
        }
        return sum;
    }
    
    public double cross2D(Vector v) {
        requireBothDimension(v, 2);
        return values.get(0) * v.values.get(1) - values.get(1) * v.values.get(0);
    }
    
      public Vector cross3D(Vector v) {
        requireBothDimension(v, 3);
 
        double a1 = values.get(0), a2 = values.get(1), a3 = values.get(2);
        double b1 = v.values.get(0), b2 = v.values.get(1), b3 = v.values.get(2);
 
        List<Double> result = List.of(
                a2 * b3 - a3 * b2,
                a3 * b1 - a1 * b3,
                a1 * b2 - a2 * b1);
        return new Vector(result) {}; //DONT REMOVE CURLY THEY ARE FOR ABSTRACTS
    }
      
      public Vector projectOn(Vector v) {
        validateDimensions(v);
 
        double vDotV = v.dotProduct(v);
        if (vDotV == 0.0) {
            throw new ArithmeticException("Cannot project onto the zero vector.");
        }
 
        double factor = this.dotProduct(v) / vDotV;
        
        //This logic makes sense, i will find bug later/a fix later
        Vector result = new Vector(v);   // copy first so v itself is never called
        result.scalarMult(factor);
        return result;
    }
    
    private void validateDimensions(Vector v) {
        Objects.requireNonNull(v, "The other vector cannot be null.");
        if (this.dimension != v.dimension) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimension, but got "
                            + this.dimension + " and " + v.dimension + ".");
        }
    }
    
    /**
     * exists because I was too lazy to write it twice
     * @param v is the vector v
     * @param required how many you need
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
     * the method that will fix my TODO on how to check the validity of some 
     * actions applied to vectors
     * @param source the new component(S)
     * @return the new List of values
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
    
    
}
