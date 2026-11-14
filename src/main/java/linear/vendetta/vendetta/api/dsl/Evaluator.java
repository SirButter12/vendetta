package linear.vendetta.vendetta.api.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.SystemMatrix;
import linear.vendetta.vendetta.api.linear_algebra.Vector;
import linear.vendetta.vendetta.api.dsl.Node.NodeKind;
import static linear.vendetta.vendetta.api.dsl.evaluators.MathEvaluator.*;
import static linear.vendetta.vendetta.api.dsl.evaluators.MatrixEvaluator.*;
import static linear.vendetta.vendetta.api.dsl.evaluators.ArithmeticEvaluator.*;
import static linear.vendetta.vendetta.api.dsl.evaluators.VectorEvaluator.*;

/**
 * Walks a Node tree and computes its value (Double, Vector or Matrix).
 * The actual operations live in the evaluators package.
 *
 * @author ian
 */
public class Evaluator {
    private static HashMap<String, Object> memory = Interpreter.getMemory();   // same map as Interpreter

    /**
     * Evaluates a node and its children recursively.
     *
     * @throws CommandException on any error (DECLARE nodes can't be evaluated)
     */
    public static Object evaluateNode(Node root) {

        return switch (root.kind()) {
            case NodeKind.ASSIGN -> assign(root);

            case NodeKind.BINARY -> bin(root);

            case NodeKind.CALL -> call(root);

            case NodeKind.NAME -> lookup(root.text());

            case NodeKind.NUMBER -> Double.parseDouble(root.text());

            case NodeKind.LIST -> list(root);

            case NodeKind.DECLARE -> throw new CommandException(
                    "Matrix and Vector constructors are filled in by hand, they cannot be evaluated");
        };

    }

    /** DECLARE node -> zero Matrix/Vector of the declared size. */
    public static Object buildEmpty(Node declaration) {
        Node constructor = declaration.children().get(0);

        List<Object> sizes = new ArrayList<>();
        for (Node child : constructor.children()) {
            sizes.add(evaluateNode(child));
        }

        return constructor.text().equals("Matrix") ? matrix(sizes) : vector(sizes);
    }

    /** DECLARE node -> text like "Matrix(2, 3)", for error messages. */
    public static String describe(Node declaration) {
        Node constructor = declaration.children().get(0);
        List<String> sizes = new ArrayList<>();
        for (Node child : constructor.children()) {
            sizes.add(String.valueOf(toSize(evaluateNode(child))));
        }
        return constructor.text() + "(" + String.join(", ", sizes) + ")";
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

    /**
     * Value of a variable.
     *
     * @throws CommandException if it does not exist
     */
    private static Object lookup(String name) {
        Object value = memory.get(name);
        if (value == null) {
            throw new CommandException(name + " is used but never initialized!");
        }
        return value;
    }

    /** x = expr: evaluates, saves in memory and returns the value. */
    private static Object assign(Node root) {
        Object value = evaluateNode(root.children().get(0));

        Interpreter.assignToMemory(root.text(), value);

        return value;
    }

    /** Binary operator: evaluates both sides, then delegates to ArithmeticEvaluator. */
    private static Object bin(Node node) {
        Object left  = evaluateNode(node.children().get(0));
        Object right = evaluateNode(node.children().get(1));

        return switch(node.text()) {
            case "+" -> sum(left, right);
            case "-" -> sub(left, right);
            case "*" -> times(left, right);
            case "/" -> div(left, right);
            case "^" -> exp(left, right);
            default -> throw new IllegalStateException("Unexpected value: " + (node.text()));
        };
    }

    /**
     * Function call: evaluates the arguments, then dispatches by name.
     * To add a function: add its name to Tokenizer.functions and a case here.
     */
    private static Object call(Node node) {
        List<Object> args = new ArrayList<>();

        for (Node child : node.children()) {
            args.add(evaluateNode(child));
        }

        return switch(node.text()) {
            case "adj" -> adj(args);
            case "inv" -> inv(args);
            case "trans" -> trans(args);
            case "det" -> det(args);
            case "dot" -> dot(args);
            case "cross" -> cross(args);
            case "project" -> project(args);
            case "solveSystem" -> solveSystem(args);
            case "gaussJordan" -> gaussJordan(args);
            case "triangulize" -> triangulize(args);

            //MATH
            case "sin" -> sin(args);
            case "cos" -> cos(args);
            case "tan" -> tan(args);
            case "sec" -> sec(args);
            case "csc" -> csc(args);
            case "cot" -> cot(args);
            case "arcsin" -> arcsin(args);
            case "arccos" -> arccos(args);
            case "arctan" -> arctan(args);
            case "arcsec" -> arcsec(args);
            case "arccsc" -> arccsc(args);
            case "arccot" -> arccot(args);
            case "ln" -> ln(args);
            case "sqrt" -> sqrt(args);
            case "log" -> log(args);
            case "log10" -> log10(args);
            case "mod" -> mod(args);


            default -> throw new IllegalStateException("Unexpected value: " + (node.text()));
        };
    }

    /** solveSystem(A, b): solves Ax = b using the inverse. */
    private static Object solveSystem(List<Object> args) {
        expectArgs("solveSystem", args, 2);

        if (args.get(0) instanceof Matrix m && args.get(1) instanceof Vector v) {
            SystemMatrix systemMatrix = new SystemMatrix(m, v);

            return systemMatrix.solve();
        }

        throw new CommandException("Invalid arguments");
    }

    /** gaussJordan(A, b): solves Ax = b with Gauss-Jordan and prints the steps to stdout. */
    private static Object gaussJordan(List<Object> args) {
        expectArgs("gaussJordan", args, 2);

        if (args.get(0) instanceof Matrix m && args.get(1) instanceof Vector v) {
            SystemMatrix systemMatrix = new SystemMatrix(m, v);

            Vector s = systemMatrix.gaussJordan();

            for (String step: systemMatrix.getGaussSteps()) {
                System.out.println(step);
            }

            return s;
        }

        throw new CommandException("Invalid arguments");
    }

    /**
     * [...] -> Vector if all elements are numbers,
     * Matrix if all are Vectors (each one is a row).
     */
    private static Object list(Node node) {
        List<Object> values = new ArrayList<>();
        for (Node child : node.children()) {
            values.add(evaluateNode(child));
        }

        if (values.stream().allMatch(o -> o instanceof Double)) {
            List<Double> numbers = new ArrayList<>();
            for (Object o : values) {
                double d = (Double) o;
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    throw new CommandException("Element " + numbers.size() + " of the vector is not a valid number");
                }
                numbers.add(d);
            }
            return new Vector(numbers);
        }

        if (values.stream().allMatch(o -> o instanceof Vector)) {
            List<Vector> rows = new ArrayList<>();
            for (Object o : values) {
                rows.add((Vector) o);
            }
            try {
                return Matrix.build(rows);
            } catch (IllegalArgumentException e) {
                throw new CommandException("Every row of a matrix must have the same size");
            }
        }

        throw new CommandException("A list must contain only numbers (vector) or only vectors (matrix rows)");
    }

    /** @throws CommandException if args does not have exactly n elements */
    public static void expectArgs(String fn, List<Object> args, int n) {
        if (args.size() != n) {
            throw new CommandException(fn + " needs " + n + " argument(s)");
        }
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
}