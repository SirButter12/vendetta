package linear.vendetta.vendetta.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import linear.vendetta.vendetta.api.dsl.CommandException;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.PolarForm;
import linear.vendetta.vendetta.api.linear_algebra.Vector;

/**
 * Small static helpers shared by several classes of the API:
 * argument checks for the language functions, number formatting
 * and the text of row operations.
 *
 * Only static methods; it is never instantiated.
 *
 * @author ian
 */
public final class helpers {

    private helpers() { }



    //---ARGUMENT CHECKS (used by Evaluator and the evaluators/ classes)---

    /** @throws CommandException if args does not have exactly n elements */
    public static void expectArgs(String fn, List<Object> args, int n) {
        if (args.size() != n) {
            throw new CommandException(fn + " needs " + n + " argument(s)");
        }
    }

    /** @throws CommandException if args has fewer than n elements */
    public static void expectMinArgs(String fn, List<Object> args, int n) {
        if (args.size() < n) {
            throw new CommandException(fn + " needs at least " + n + " argument(s)");
        }
    }

    /**
     * Error for a function called with values of the wrong type, e.g.
     * "det(M) needs a matrix M, but it got a vector".
     *
     * @param usage how the function is written, e.g. "det(M)"
     * @param expected what it needs, e.g. "a matrix M"
     */
    public static CommandException wrongArgs(String usage, String expected, List<Object> args) {
        List<String> got = new ArrayList<>();
        for (Object o : args) {
            got.add(typeName(o));
        }
        return new CommandException(usage + " needs " + expected + ", but it got " + String.join(" and ", got));
    }

    /** "a number", "a vector", "a matrix", "a polar form" (for error messages). */
    public static String typeName(Object o) {
        if (o instanceof Double) return "a number";
        if (o instanceof Matrix) return "a matrix";
        if (o instanceof Vector) return "a vector";
        if (o instanceof PolarForm) return "a polar form";
        return o == null ? "nothing" : o.getClass().getSimpleName();
    }

    /**
     * Value -> positive whole number (for sizes).
     *
     * @throws CommandException otherwise
     */
    public static int toSize(Object o) {
        if (o instanceof Double d && d == Math.floor(d) && d > 0) {
            return d.intValue();
        }
        throw new CommandException("Sizes must be whole positive numbers");
    }

    /**
     * True if both are Matrices with the same rows x cols,
     * or both are Vectors with the same dimension.
     */
    public static boolean sameShape(Object a, Object b) {
        if (a instanceof Matrix m1 && b instanceof Matrix m2) {
            return m1.getNumRows() == m2.getNumRows() && m1.getNumCols() == m2.getNumCols();
        }

        if (a instanceof Vector v1 && b instanceof Vector v2) {
            return v1.getDimension() == v2.getDimension();
        }

        return false;
    }



    //---FORMATTING (used by Vector, Matrix, PolarForm, SystemMatrix and the UI)---

    /** Max decimals shown by formatNumber(). Display only. */
    private static final int DISPLAY_DECIMALS = 4;

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



    //---ROW OPERATION TEXT (used by Matrix and TransformableVector)---

    /** "R_i -> R_i * a" */
    public static String rowMultiplyStep(double a, int idx) {
        return String.format("R_%d -> R_%d * %s\n", idx, idx, formatNumber(a));
    }

    /** "R_i <-> R_j" */
    public static String rowSwapStep(int idx1, int idx2) {
        return String.format("R_%d <-> R_%d\n", idx1, idx2);
    }

    /** "R_i -> R_i + k * R_j" */
    public static String rowCompStep(int idx1, int idx2, double k) {
        return String.format("R_%d -> R_%d + %s * R_%d\n", idx1, idx1, formatNumber(k), idx2);
    }
}
