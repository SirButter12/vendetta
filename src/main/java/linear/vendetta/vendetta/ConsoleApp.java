package linear.vendetta.vendetta;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import linear.vendetta.vendetta.api.dsl.CompiledResult;
import linear.vendetta.vendetta.api.dsl.Interpreter;

/**
 * Console version for testing
 * 
 * @author Ian
 */
public class ConsoleApp {

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String line : args) {
                System.out.println("v: " + line);
                runCommand(line);
            }
            return;
        }

        printWelcome();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nv: ");

            if (!scanner.hasNextLine()) {
                break;
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }

            switch (line.split("\\s+")[0].toLowerCase()) {
                case "exit", "quit" -> {
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;
                }
                case "help" -> printHelp();
                case "memory" -> printMemory();
                default -> runCommand(line);
            }
        }
        scanner.close();
    }

    private static void runCommand(String command) {
        try {
            List<CompiledResult> results = Interpreter.interprete(List.of(command));
            for (CompiledResult result : results) {
                System.out.println(result.getResult());
            }
        } catch (Exception e) {
            String message = (e.getMessage() != null) ? e.getMessage() : e.getClass().getSimpleName();
            System.out.println("Error: " + message);
        }
    }

    private static void printWelcome() {
        System.out.println("==============================================");
        System.out.println("        LINEAR ALGEBRA INTERPRETER");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("Type 'help' for help, 'memory' to see stored");
        System.out.println("variables, or 'exit' to quit.");
        printMemory();
    }

    private static void printHelp() {
        System.out.println("================ HELP ================");
        System.out.println("Type one command per line and press Enter.");
        System.out.println("Spaces are optional: 'A+B' and 'A + B' are the same.");
        System.out.println("Function names are case-sensitive and must be written exactly as shown.");

        System.out.println("\n--- Values ---");
        System.out.println("  3, 2.5            Numbers (decimals use a dot, no 1e3 or .5)");
        System.out.println("  A, M2, result     Variables: a letter followed by letters or digits");
        System.out.println("  pi, e             Constants (cannot be reassigned)");
        System.out.println("  i                 2x2 rotation matrix [[0, -1], [1, 0]] (cannot be reassigned)");

        System.out.println("\n--- Vectors and matrices ---");
        System.out.println("  [1, 2, 3]                  Vector");
        System.out.println("  [[1, 2], [3, 4]]           Matrix: each inner list is a row");
        System.out.println("  [[1, 2, 3], [1, 3, 4], v]  Rows can be vector variables");
        System.out.println("  [sqrt(2), pi/2, sin(3)]    Each element can be any expression that gives a number");
        System.out.println("  [2*A, A + v]               Each row can be any expression that gives a vector");
        System.out.println("                             All rows must have the same size");

        System.out.println("\n--- Operators ---");
        System.out.println("  A + B      Add two numbers, vectors or matrices of the same size");
        System.out.println("  A - B      Subtract two numbers, vectors or matrices of the same size");
        System.out.println("  2 * A      Multiply a vector or matrix by a number (either order)");
        System.out.println("  A * B      Vector * vector = dot product | Matrix * matrix = matrix product");
        System.out.println("  M * v      Matrix * vector = vector (v * M is not allowed)");
        System.out.println("  A / 2      Divide a vector or matrix by a number");
        System.out.println("  M / N      Matrix division: M * inv(N)");
        System.out.println("  x ^ y      Power of a number. 2^3^2 = 2^(3^2)");
        System.out.println("  M ^ k      Power of a square matrix, k a whole number (negative uses the inverse)");
        System.out.println("  -A         Negative of a number, vector or matrix. -2^2 = -4");
        System.out.println("  ( )        Group operations: (A + B) * 2");

        System.out.println("\n--- Vector functions ---");
        System.out.println("  dot(u, v)        Dot product (a number)");
        System.out.println("  cross(u, v)      Cross product: 2D gives a number, 3D gives a vector");
        System.out.println("  project(u, v)    Projection of u onto v");

        System.out.println("\n--- Matrix functions ---");
        System.out.println("  det(M)           Determinant (square matrices only)");
        System.out.println("  inv(M)           Inverse (square matrices with det != 0)");
        System.out.println("  adj(M)           Adjugate (square matrices only)");
        System.out.println("  trans(M)         Transpose");
        System.out.println("  triangulize(M)   Triangular (row echelon) form, using Gauss");
        System.out.println("  solveSystem(M, v) Solve the system M x = v using matrix inverse ");
        System.out.println("  gaussJordan(M, v) Solve the system M x = v using row reduction to RREF, the steps to do so are printed ");
        
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
        System.out.println("  M = Matrix(n, m) / v = Vector(n) are for the graphic version, where you fill");
        System.out.println("                   the values by hand. In the console use [ ] instead.");

        System.out.println("\n--- Console commands ---");
        System.out.println("  memory           List every stored variable and its value");
        System.out.println("  help             Show this message");
        System.out.println("  exit             Close the program");
        System.out.println("======================================");
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
            System.out.println(value);
            System.out.println();
        }
    }
}
