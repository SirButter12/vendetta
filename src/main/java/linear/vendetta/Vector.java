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
        //TODO: Add a check for if it will be a valid vector
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
        //TODO: Add a check for if it will be a valid change
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
    
    private void validateDimensions(Vector v) {
        Objects.requireNonNull(v, "The other vector cannot be null.");
        if (this.dimension != v.dimension) {
            throw new IllegalArgumentException(
                    "Vectors must have the same dimension, but got "
                            + this.dimension + " and " + v.dimension + ".");
        }
    }
    
}
