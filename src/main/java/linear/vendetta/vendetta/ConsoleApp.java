package linear.vendetta.vendetta;

import java.util.List;
import java.util.Map;
import java.util.Scanner;
import linear.vendetta.vendetta.Interpreter.CompiledResult;
import linear.vendetta.vendetta.Interpreter.Interpreter;

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
            List<CompiledResult> results = Interpreter.compile(List.of(command));
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
        System.out.println("  3, 2.5            Numbers (decimals use a dot)");
        System.out.println("  A, M2, result     Variables: a letter followed by letters or digits");

        System.out.println("\n--- Operators ---");
        System.out.println("  A + B      Add two numbers, vectors or matrices of the same size");
        System.out.println("  A - B      Subtract two numbers, vectors or matrices of the same size");
        System.out.println("  2 * A      Multiply a vector or matrix by a number (either order)");
        System.out.println("  A * B      Vector * vector = dot product | Matrix * matrix = matrix product");
        System.out.println("  A / 2      Divide a vector or matrix by a number");
        System.out.println("  M / N      Matrix division: M * inv(N)");
        System.out.println("  -A         Negative of a number, vector or matrix");
        System.out.println("  ( )        Group operations: (A + B) * 2");

        System.out.println("\n--- Vector functions ---");
        System.out.println("  dot(u, v)        Dot product (a number)");
        System.out.println("  cross(u, v)      Cross product: 2D gives a number, 3D gives a vector");
        System.out.println("  project(u, v)    Projection of u onto v");
        System.out.println("  Vector(n)        New vector of size n filled with zeros");

        System.out.println("\n--- Matrix functions ---");
        System.out.println("  det(M)           Determinant (square matrices only)");
        System.out.println("  inv(M)           Inverse (square matrices only)");
        System.out.println("  adj(M)           Adjugate");
        System.out.println("  trans(M)         Transpose");
        System.out.println("  triangulize(M)   Triangular form");
        System.out.println("  Matrix(n, m)     New n x m matrix filled with zeros");
        System.out.println("  solveSystem(m, v) Solve a system of equations (coming soon)");

        System.out.println("\n--- Number functions ---");
        System.out.println("  sin cos tan sec csc cot ln sqrt");

        System.out.println("\n--- Variables ---");
        System.out.println("  x = expression   Saves the result in a variable. Example: V = C + D");
        System.out.println("                   The assignment must be at the start of the command.");
        System.out.println("  Functions can be combined: det(trans(M)), dot(A + B, A)");

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
