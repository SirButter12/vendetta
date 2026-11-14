package linear.vendetta.vendetta.Interpreter.evaluators;

import java.util.List;
import linear.vendetta.vendetta.API.Vector;
import linear.vendetta.vendetta.Interpreter.CommandException;
import static linear.vendetta.vendetta.Interpreter.Evaluator.expectArgs;
import static linear.vendetta.vendetta.Interpreter.Evaluator.toSize;

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
}
