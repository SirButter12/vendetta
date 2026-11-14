package linear.vendetta.vendetta.Interpreter.evaluators;

import java.util.List;
import linear.vendetta.vendetta.Interpreter.CommandException;
import static linear.vendetta.vendetta.Interpreter.Evaluator.expectArgs;

/**
 * Scalar math functions. Each one checks the argument count and type,
 * then returns a double. Angles are in radians.
 *
 * @author ian
 */
public class MathEvaluator {
    /** sin(x) */
    public static double sin(List<Object> args) {
        expectArgs("sin", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.sin(x);
        }

        throw new CommandException("invalid argument");
    }

    /** cos(x) */
    public static double cos(List<Object> args) {
        expectArgs("cos", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.cos(x);
        }

        throw new CommandException("invalid argument");
    }

    /** tan(x) */
    public static double tan(List<Object> args) {
        expectArgs("tan", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.tan(x);
        }

        throw new CommandException("invalid argument");
    }

    /** sec(x) = 1 / cos(x) */
    public static double sec(List<Object> args) {
        expectArgs("sec", args, 1);

        if (args.get(0) instanceof Double x) {
            return 1 / Math.cos(x);
        }

        throw new CommandException("invalid argument");
    }

    /** csc(x) = 1 / sin(x) */
    public static double csc(List<Object> args) {
        expectArgs("csc", args, 1);

        if (args.get(0) instanceof Double x) {
            return 1 / Math.sin(x);
        }

        throw new CommandException("invalid argument");
    }

    /** cot(x) = 1 / tan(x) */
    public static double cot(List<Object> args) {
        expectArgs("cot", args, 1);

        if (args.get(0) instanceof Double x) {
            return 1 / Math.tan(x);
        }

        throw new CommandException("invalid argument");
    }

    /** arcsin(x), x in [-1, 1] */
    public static double arcsin(List<Object> args) {
        expectArgs("arcsin", args, 1);

        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) > 1) {
                throw new CommandException("arcsin is only defined for -1 <= x <= 1");
            }
            return Math.asin(x);
        }

        throw new CommandException("invalid argument");
    }

    /** arccos(x), x in [-1, 1] */
    public static double arccos(List<Object> args) {
        expectArgs("arccos", args, 1);

        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) > 1) {
                throw new CommandException("arccos is only defined for -1 <= x <= 1");
            }
            return Math.acos(x);
        }

        throw new CommandException("invalid argument");
    }

    /** arctan(x) */
    public static double arctan(List<Object> args) {
        expectArgs("arctan", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.atan(x);
        }

        throw new CommandException("invalid argument");
    }

    /** arcsec(x) = arccos(1/x), |x| >= 1 */
    public static double arcsec(List<Object> args) {
        expectArgs("arcsec", args, 1);

        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) < 1) {
                throw new CommandException("arcsec is only defined for |x| >= 1");
            }
            return Math.acos(1 / x);
        }

        throw new CommandException("invalid argument");
    }

    /** arccsc(x) = arcsin(1/x), |x| >= 1 */
    public static double arccsc(List<Object> args) {
        expectArgs("arccsc", args, 1);

        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) < 1) {
                throw new CommandException("arccsc is only defined for |x| >= 1");
            }
            return Math.asin(1 / x);
        }

        throw new CommandException("invalid argument");
    }

    /** arccot(x) = pi/2 - arctan(x), result in (0, pi) */
    public static double arccot(List<Object> args) {
        expectArgs("arccot", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.PI / 2 - Math.atan(x);
        }

        throw new CommandException("invalid argument");
    }

    /** log(x, base). Base must be positive and != 1. */
    public static double log(List<Object> args) {
        expectArgs("log", args, 2);

        if (args.get(0) instanceof Double x && args.get(1) instanceof Double base) {
            if (base <= 0 || base == 1) {
                throw new CommandException("log base must be positive and different from 1");
            }
            return Math.log(x) / Math.log(base);
        }

        throw new CommandException("invalid argument");
    }

    /** log10(x). Negative x gives NaN (not checked). */
    public static double log10(List<Object> args) {
        expectArgs("log10", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.log10(x);
        }

        throw new CommandException("invalid argument");
    }

    /** mod(a, b): result has the sign of b, e.g. mod(-1, 3) = 2. b != 0. */
    public static double mod(List<Object> args) {
        expectArgs("mod", args, 2);

        if (args.get(0) instanceof Double a && args.get(1) instanceof Double b) {
            if (b == 0) {
                throw new CommandException("mod by zero is not defined");
            }
            return ((a % b) + b) % b;
        }

        throw new CommandException("invalid argument");
    }

    /** ln(x), natural log. Negative x gives NaN (not checked). */
    public static double ln(List<Object> args) {
        expectArgs("ln", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.log(x);
        }

        throw new CommandException("invalid argument");
    }

    /** sqrt(x). Negative x gives NaN (not checked). */
    public static double sqrt(List<Object> args) {
        expectArgs("sqrt", args, 1);

        if (args.get(0) instanceof Double x) {
            return Math.sqrt(x);
        }

        throw new CommandException("invalid argument");
    }
}
