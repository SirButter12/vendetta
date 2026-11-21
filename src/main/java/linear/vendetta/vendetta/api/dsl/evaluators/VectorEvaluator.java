package linear.vendetta.vendetta.api.dsl.evaluators;

import java.util.ArrayList;
import java.util.List;
import linear.vendetta.vendetta.api.linear_algebra.Vector;
import linear.vendetta.vendetta.api.dsl.CommandException;
import static linear.vendetta.vendetta.api.dsl.Evaluator.expectArgs;
import static linear.vendetta.vendetta.api.dsl.Evaluator.expectMinArgs;
import static linear.vendetta.vendetta.api.dsl.Evaluator.toSize;
import linear.vendetta.vendetta.api.linear_algebra.PolarForm;

/**
 * Vector functions callable from commands.
 *
 * @author ian
 */
public class VectorEvaluator {
    /** cross(a, b): 2D -> number (a1*b2 - a2*b1), 3D -> Vector. */
    public static Object cross(List<Object> args) {
        expectArgs("cross", args, 2);
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {

            if (v1.getDimension() == 2 && v2.getDimension() == 2) {
                return v1.cross2D(v2);
            }

            else if (v1.getDimension() == 3 && v2.getDimension() == 3) {
                return v1.cross3D(v2);
            }

            throw new CommandException("Invalid vector dimension");
        }

        throw new CommandException("Invalid argument for function");
    }

    /** project(a, b): projection of a onto b. */
    public static Vector project(List<Object> args) {
        expectArgs("project", args, 2);
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {


            return v1.projectOn(v2);
        }

        throw new CommandException("Invalid argument for function");
    }

    /** Vector(n) declaration -> zero vector of size n. */
    public static Object vector(List<Object> args) {
        expectArgs("vector", args, 1);
        return new Vector(toSize(args.get(0)));
    }
    
    public static Object length(List<Object> args) {
        expectArgs("length", args, 1);
        
        if (args.get(0) instanceof Vector v) {
            return v.getLen();
        }
        
        throw new CommandException("Invalid argument for function");
    }

    public static Object direction(List<Object> args) {
        expectArgs("direction", args, 1);
        
        if (args.get(0) instanceof Vector v) {
            try {
                return new Vector(v.getDirection()); //provicional
            } catch (ArithmeticException e) {
                throw new CommandException("The zero vector has no direction");
            }
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
        /**
     * polarV(r, t1, ..., tk) -> Cartesian Vector of k+1 components (angles in radians).
     * polarV(r) -> [r]. polarV(polarize(v)) -> v.
     */
    public static Vector polarV(List<Object> args) {
        expectMinArgs("polarV", args, 1);

        if (args.size() == 1 && args.get(0) instanceof PolarForm p) {
            return p.toVector();
        }

        List<Double> numbers = new ArrayList<>();
        for (Object o : args) {
            if (!(o instanceof Double d) || d.isNaN() || d.isInfinite()) {
                throw new CommandException("polarV only takes numbers: polarV(length, angle1, angle2, ...)");
            }
            numbers.add(d);
        }

        try {
            return Vector.fromPolar(numbers.get(0), numbers.subList(1, numbers.size()));
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage());
        }
    }

    /** polarize(v) -> {length: r, angles: [t1, ...]}. */
    public static PolarForm polarize(List<Object> args) {
        expectArgs("polarize", args, 1);

        if (args.get(0) instanceof Vector v) {
            try {
                return v.toPolar();
            } catch (ArithmeticException e) {
                throw new CommandException("The zero vector has no polar form");
            }
        }

        throw new CommandException("Invalid argument for function");
    }
}
