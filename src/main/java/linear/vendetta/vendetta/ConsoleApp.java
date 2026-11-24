package linear.vendetta.vendetta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import linear.vendetta.vendetta.api.Helpers;
import linear.vendetta.vendetta.api.dsl.CompiledResult;
import linear.vendetta.vendetta.api.dsl.Interpreter;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.SolutionVector;
import linear.vendetta.vendetta.api.linear_algebra.Vector;

/**
 * Console version of Vendetta. Started with {@code App --console}.
 *
 * Interactive mode: one command per line.
 * Script mode: {@code App --console "A = [[1, 2], [3, 4]]" "det(A)"} runs each argument as a command.
 *
 * Declarations ({@code A = Matrix(2, 2)}, {@code v = Vector(3)}) ask for their values
 * row by row, like the cells of the graphical version.
 *
 * @author Ian
 */
public class ConsoleApp {

    /** Input of the interactive mode; null in script mode (no one to ask for values). */
    private static Scanner scanner;

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String line : args) {
                System.out.println("v: " + line);
                runCommand(line);
            }
            return;
        }

        printWelcome();
        scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nv: ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            switch (line.toLowerCase()) {
                case "exit", "quit" -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                }
                case "help" -> printHelp();
                case "memory" -> printMemory();
                case "clear", "reset" -> clearMemory();
                default -> runCommand(line);
            }
        }
        scanner.close();
    }

    // ------------------------------------------------------------------
    // Running commands
    // ------------------------------------------------------------------

    /** Runs one command and prints its result, or the error. Never throws. */
    private static void runCommand(String command) {
        try {
            // prepareVariables adds new declared names to memory (zero-filled), so remember
            // what existed before, to undo that if the user cancels.
            boolean[] isNew = new boolean[1];
            String declared = declaredName(command, isNew);
            if (declared != null) {
                runDeclaration(command, declared, isNew[0]);
                return;
            }

            List<CompiledResult> results = Interpreter.interprete(List.of(command));
            for (CompiledResult result : results) {
                printResult(result.getResult());
            }
        } catch (Exception e) {
            System.out.println("Error: " + errorMessage(e));
        }
    }

    /**
     * Name declared by {@code NAME = Matrix(n, m)} / {@code NAME = Vector(n)}, or null
     * if the command is not a declaration. The interpreter decides, not this class.
     */
    private static String declaredName(String command, boolean[] isNew) {
        Set<String> before = new HashSet<>(Interpreter.getMemory().keySet());
        Map<String, Object> templates = Interpreter.prepareVariables(List.of(command));
        if (templates.isEmpty()) {
            return null;
        }
        String name = templates.keySet().iterator().next();
        isNew[0] = !before.contains(name);
        return name;
    }

    /**
     * Asks for the values of a declared matrix or vector, then loads them
     * through {@code Interpreter.interprete(commands, manualValues)}.
     */
    private static void runDeclaration(String command, String name, boolean isNew) {
        if (scanner == null) {
            if (isNew) {
                Interpreter.getMemory().remove(name);
            }
            throw new IllegalStateException(name + " is a declaration and its values are typed by hand,"
                    + " which needs the interactive console. In a script use [ ] instead,"
                    + " e.g. " + name + " = [[1, 2], [3, 4]]");
        }

        Object template = Interpreter.prepareVariables(List.of(command)).get(name);
        Object value;
        if (template instanceof Matrix m) {
            value = readMatrix(name, m.getNumRows(), m.getNumCols());
        } else {
            value = readVector(name, ((Vector) template).getDimension());
        }

        if (value == null) {
            if (isNew) {
                Interpreter.getMemory().remove(name);
            }
            System.out.println("Cancelled. " + name + " was not changed.");
            return;
        }

        Map<String, Object> manualValues = new LinkedHashMap<>();
        manualValues.put(name, value);
        Interpreter.interprete(List.of(command), manualValues);

        System.out.println(name + " =");
        printResult(Interpreter.getMemory().get(name));
    }

    /** Reads a rows x cols matrix, one row per line. Returns null if cancelled. */
    private static Matrix readMatrix(String name, int rows, int cols) {
        System.out.println("Enter the " + plural(rows, "row") + " of " + name + ", " + plural(cols, "value")
                + (rows > 1 ? " each" : "") + ", separated by commas (or 'cancel').");
        List<Vector> rowVectors = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            List<Double> row = readValues("  row " + (r + 1) + ": ", cols);
            if (row == null) {
                return null;
            }
            rowVectors.add(new Vector(row));
        }
        return Matrix.build(rowVectors);
    }

    /** Reads a vector of the given dimension in one line. Returns null if cancelled. */
    private static Vector readVector(String name, int dimension) {
        System.out.println("Enter the " + plural(dimension, "component") + " of " + name
                + ", separated by commas (or 'cancel').");
        List<Double> values = readValues("  " + name + ": ", dimension);
        return values == null ? null : new Vector(values);
    }

    /**
     * Reads one line with exactly {@code count} comma-separated values. Each value can be
     * any expression that gives a number ({@code 3}, {@code -1.5}, {@code sqrt(2)/2}, {@code pi/4}).
     * Asks again until the line is valid. Returns null on 'cancel' or end of input.
     */
    private static List<Double> readValues(String prompt, int count) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("cancel")) {
                return null;
            }

            String[] parts = line.split(",");
            if (parts.length != count) {
                System.out.println("  Expected " + plural(count, "value") + " but got " + parts.length + ". Try again.");
                continue;
            }

            try {
                List<Double> values = new ArrayList<>();
                for (String part : parts) {
                    values.add(Interpreter.evaluateEntry(part));
                }
                return values;
            } catch (Exception e) {
                System.out.println("  " + errorMessage(e).replaceAll("[.!]+$", "") + ". Try again.");
            }
        }
    }

    // ------------------------------------------------------------------
    // Printing
    // ------------------------------------------------------------------

    /**
     * Prints any result. gaussJordan results also print their steps;
     * numbers go through Helpers.formatNumber (2 instead of 2.0).
     */
    private static void printResult(Object result) {
        if (result instanceof SolutionVector s) {
            System.out.println("Steps:");
            for (String step : s.getSteps()) {
                System.out.println("  " + step.strip());
            }
            System.out.println("Solution:");
            System.out.println(new Vector(s));
        } else if (result instanceof Double d) {
            System.out.println(Helpers.formatNumber(d));
        } else {
            System.out.println(result);
        }
    }

    /** plural(1, "row") = "1 row", plural(3, "row") = "3 rows". */
    private static String plural(int n, String word) {
        return n + " " + word + (n == 1 ? "" : "s");
    }

    private static String errorMessage(Exception e) {
        return (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
    }

    private static void printWelcome() {
        System.out.println("==============================================");
        System.out.println("        VENDETTA - LINEAR ALGEBRA CONSOLE");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("Type 'help' for help, 'memory' to see stored");
        System.out.println("variables, 'clear' to delete them, or 'exit' to quit.");
    }

    private static void printHelp() {
        System.out.println("================ HELP ================");
        System.out.println("Type one command per line and press Enter.");
        System.out.println("Spaces are optional: 'A+B' and 'A + B' are the same.");
        System.out.println("Function names are case-sensitive and must be written exactly as shown.");

        System.out.println("\n--- Values ---");
        System.out.println("  3, 2.5, -4        Numbers (decimals use a dot: write 0.5, not .5)");
        System.out.println("  1e3, 2.5E-4       Scientific notation");
        System.out.println("  A, M2, result     Variables: a letter followed by letters or digits");
        System.out.println("  pi, e             Constants (cannot be reassigned)");

        System.out.println("\n--- Vectors and matrices ---");
        System.out.println("  [1, 2, 3]                  Vector");
        System.out.println("  [[1, 2], [3, 4]]           Matrix: each inner list is a row");
        System.out.println("  [[1, 2, 3], [1, 3, 4], v]  Rows can be vector variables");
        System.out.println("  [sqrt(2), pi/2, sin(3)]    Each element can be any expression that gives a number");
        System.out.println("  [2*A, A + v]               Each row can be any expression that gives a vector");
        System.out.println("                             All rows must have the same size");
        System.out.println("  M = Matrix(n, m)           Declares an n x m matrix and asks for its values row by row");
        System.out.println("  v = Vector(n)              Declares a vector of n components and asks for its values");
        System.out.println("                             Values are separated by commas: 1, sqrt(2)/2, -3");

        System.out.println("\n--- Operators ---");
        System.out.println("  A + B      Add two numbers, vectors or matrices of the same size");
        System.out.println("  A - B      Subtract two numbers, vectors or matrices of the same size");
        System.out.println("  2 * A      Multiply a vector or matrix by a number (either order)");
        System.out.println("  A * B      Vector * vector = dot product | Matrix * matrix = matrix product");
        System.out.println("  M * v      Matrix * vector = vector (v * M is not allowed)");
        System.out.println("  A / 2      Divide a number, vector or matrix by a number (not by 0)");
        System.out.println("  M / N      Matrix division: M * inv(N)");
        System.out.println("  x ^ y      Power of a number. 2^3^2 = 2^(3^2)");
        System.out.println("  M ^ k      Power of a square matrix, k a whole number (negative uses the inverse)");
        System.out.println("  -A         Negative of a number, vector or matrix. -2^2 = -4");
        System.out.println("  ( )        Group operations: (A + B) * 2");

        System.out.println("\n--- Vector functions ---");
        System.out.println("  dot(u, v)        Dot product (a number). Also works for matrix-matrix and matrix-vector");
        System.out.println("  cross(u, v)      Cross product: 2D gives a number, 3D gives a vector");
        System.out.println("  project(u, v)    Projection of u onto v");
        System.out.println("  length(v)        Length (norm) of v: length([3, 4]) = 5");
        System.out.println("  direction(v)     Direction angles of v in radians, as a vector (n-1 angles)");
        System.out.println("                   2D: [atan2(y, x)]. 1D: [0] or [pi]. Not defined for the zero vector");
        System.out.println("  angleInBet(u, v) Angle between u and v in radians (0 to pi). Any dimensions:");
        System.out.println("                   the smaller vector gets zeros, so [1, 2] vs [1, 2, 3] = [1, 2, 0] vs [1, 2, 3]");

        System.out.println("\n--- Polar form (angles in radians) ---");
        System.out.println("  polarV(r, t1, ..., tk)  Vector from its length and k angles (k+1 components)");
        System.out.println("                          polarV(5, 0.9273) = [3, 4], polarV(2, pi/2, pi/2) = [0, 0, 2]");
        System.out.println("  polarV(r)               1D vector [r] (r can be negative)");
        System.out.println("  polarize(v)             Polar form of v: polarize([3, 4]) = {length: 5, angles: [0.9273]}");
        System.out.println("                          1D: {length: -5, angles: []}. Not defined for the zero vector");
        System.out.println("  polarV(polarize(v))     Back to the original vector");
        System.out.println("                          A polar form can only be shown or passed to polarV,");
        System.out.println("                          it cannot be used in + - * /");

        System.out.println("\n--- Matrix functions ---");
        System.out.println("  det(M)            Determinant (square matrices only)");
        System.out.println("  inv(M)            Inverse (square matrices with det != 0)");
        System.out.println("  adj(M)            Adjugate (square matrices only)");
        System.out.println("  trans(M)          Transpose");
        System.out.println("  triangulize(M)    Triangular (row echelon) form, using Gauss");

        System.out.println("\n--- Systems of equations (M x = v) ---");
        System.out.println("  solveSystem(M, v) Solution only, using the inverse (square M with det != 0)");
        System.out.println("  gaussJordan(M, v) Solution and every row operation used (Gauss-Jordan).");
        System.out.println("                    Rows are numbered from 0: R_1 -> R_1 + -3 * R_0");
        System.out.println("                    Says whether there is no solution or infinitely many");

        System.out.println("\n--- Number functions ---");
        System.out.println("  sin cos tan sec csc cot             Trigonometry, in radians: sin(pi/2)");
        System.out.println("  arcsin arccos arctan                Inverse trigonometry");
        System.out.println("  arcsec arccsc arccot");
        System.out.println("  sqrt(x)          Square root");
        System.out.println("  ln(x)            Natural logarithm");
        System.out.println("  log10(x)         Base 10 logarithm");
        System.out.println("  log(x, b)        Logarithm of x in base b: log(8, 2) = 3");
        System.out.println("  mod(a, b)        Remainder of a / b, always positive: mod(-1, 3) = 2");
        System.out.println("  Function arguments always need parentheses: sqrt(2), not sqrt2");

        System.out.println("\n--- Variables ---");
        System.out.println("  x = expression   Saves the result in a variable. Example: V = C + D");
        System.out.println("                   The assignment must be at the start of the command.");
        System.out.println("  Functions can be combined: det(trans(M)), dot(A + B, A), det([[1, 2], [3, 4]])");

        System.out.println("\n--- Console commands ---");
        System.out.println("  memory           List every stored variable and its value");
        System.out.println("  clear            Delete every variable (constants like pi and e are kept). Also: reset");
        System.out.println("  help             Show this message");
        System.out.println("  exit             Close the program. Also: quit");
        System.out.println("======================================");
    }

    /** Deletes every variable, leaving only the built-in constants. */
    private static void clearMemory() {
        Interpreter.resetMemory();
        System.out.println("Memory cleared. Only the constants pi and e are left.");
    }

    private static void printMemory() {
        Map<String, Object> memory = Interpreter.getMemory();

        if (memory.isEmpty()) {
            System.out.println("\nMemory is empty.");
            return;
        }

        System.out.println("\n--- Memory ---");
        for (Map.Entry<String, Object> entry : memory.entrySet()) {
            Object value = entry.getValue();
            String type = (value == null) ? "null" : value.getClass().getSimpleName();
            System.out.println(entry.getKey() + " (" + type + "):");
            // A gaussJordan result: show only the solution here, not every step again
            printResult(value instanceof SolutionVector sv ? new Vector(sv) : value);
            System.out.println();
        }
    }
}