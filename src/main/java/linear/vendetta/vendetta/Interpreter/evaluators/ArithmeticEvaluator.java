package linear.vendetta.vendetta.Interpreter.evaluators;

import java.util.List;
import linear.vendetta.vendetta.API.Matrix;
import linear.vendetta.vendetta.API.Vector;
import linear.vendetta.vendetta.Interpreter.CommandException;
import linear.vendetta.vendetta.Interpreter.CommandException;
import static linear.vendetta.vendetta.Interpreter.Evaluator.expectArgs;

/**
 * Operators + - * / ^ and dot(), for every supported type combination.
 * Unsupported combinations throw CommandException.
 *
 * @author ian
 */
public class ArithmeticEvaluator {
    /** dot(a, b): Vector.Vector -> number; Matrix*Matrix or Matrix*Vector -> product. */
    public static Object dot(List<Object> args) {
        expectArgs("dot", args, 2);

        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            return v1.dotProduct(v2);
        }

        if (args.get(0) instanceof Matrix m1 && args.get(1) instanceof Matrix m2) {
            return Matrix.multiply(m1, m2);
        }

        if (args.get(0) instanceof Matrix m && args.get(1) instanceof Vector v) {
            return Matrix.multiply(m, v);
        }

        throw new CommandException("Invalid argument for function");
    }

    /** a + b. Both must be the same type (number, Vector or Matrix). */
    public static Object sum(Object o1, Object o2) {
        if (o1 instanceof Vector v1 && o2 instanceof Vector v2) {
            return v1.add(v2);
        }

        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return m1.add(m2);
        }

        if (o1 instanceof Double x && o2 instanceof Double y) {
            return x + y;
        }

        throw new CommandException("Cannot add " + o1.getClass().getSimpleName() + " to " + o2.getClass().getSimpleName());
    }

    /** a - b. Both must be the same type (number, Vector or Matrix). */
    public static Object sub(Object o1, Object o2) {
        if (o1 instanceof Vector v1 && o2 instanceof Vector v2) {
            return v1.subtract(v2);
        }

        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return m1.subtract(m2);
        }

        if (o1 instanceof Double x && o2 instanceof Double y) {
            return x - y;
        }

        throw new CommandException("Cannot subtract " + o2.getClass().getSimpleName() + " from " + o1.getClass().getSimpleName());
    }

    /**
     * a * b:
     * number * anything -> scales it; Vector * Vector -> dot product;
     * Matrix * Matrix or Matrix * Vector -> matrix product.
     */
    public static Object times(Object o1, Object o2) {
        if (o1 instanceof Double x && o2 instanceof Double y) {
            return x * y;
        }

        if (o1 instanceof Double x) {
            return scale(o2, x);
        }

        if (o2 instanceof Double y) {
            return scale(o1, y);
        }

        if (o1 instanceof Vector v1 && o2 instanceof Vector v2) {
            return v1.dotProduct(v2);
        }

        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return Matrix.multiply(m1, m2);
        }

        if (o1 instanceof Matrix m && o2 instanceof Vector v) {
            return Matrix.multiply(m, v);
        }

        throw new CommandException("Cannot multiply " + o1.getClass().getSimpleName() + " by " + o2.getClass().getSimpleName());
    }

    /**
     * a / b: number / number; anything / number -> scales by 1/b;
     * Matrix / Matrix -> a * inverse(b).
     */
    public static Object div(Object o1, Object o2) {
        if (o1 instanceof Double x && o2 instanceof Double y) {
        return x / y;
        }

        if (o2 instanceof Double x) {
            return scale(o1, 1 / x);
        }

        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return Matrix.multiply(m1, Matrix.inverse(m2));
        }

        throw new CommandException("Cannot divide " + o1.getClass().getSimpleName() + " by " + o2.getClass().getSimpleName());
    }

    /** a ^ b: number ^ number, or Matrix ^ whole number. */
    public static Object exp(Object o1, Object o2) {
        if (o1 instanceof Double x && o2 instanceof Double y) {
            return Math.pow(x, y);
        }

        if (o1 instanceof Matrix m && o2 instanceof Double a) {
            return Matrix.pow(m, a);
        }

        throw new CommandException("^ only works for numbers!!!");
    }


    /**
     * k * Matrix or k * Vector.
     *
     * @throws CommandException for anything else
     */
    public static Object scale(Object o, double k) {
        if (o instanceof Matrix m) {
            return m.scalarMult(k);
        }

        if (o instanceof Vector v) {
            return v.scalarMult(k);
        }

        throw new CommandException("Expected a matrix or a vector");
    }
}
