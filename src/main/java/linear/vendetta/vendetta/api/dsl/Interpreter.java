package linear.vendetta.vendetta.api.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.Vector;
import linear.vendetta.vendetta.api.dsl.Node.NodeKind;
import linear.vendetta.vendetta.api.dsl.Token.TokenType;

/**
 * Entry point of the interpreter. Holds the global variables and runs commands:
 * text -> Tokenizer -> Parser -> Evaluator.
 *
 * Typical flow:
 * 1. prepareVariables(commands): find Matrix/Vector declarations so the UI can ask for their values.
 * 2. compile(commands, manualValues): run everything and get one result per command.
 *
 * @author ian
 */
public class Interpreter {
    /** Global variables (name -> Double/Vector/Matrix). Static: shared by every call. */
    private static HashMap<String, Object> memory = new HashMap();
    /** Names that cannot be reassigned. */
    private static final Set<String> CONSTANTS = Set.of("pi", "e", "i");

    // Demo variables + constants.
    // Note: i is the 2x2 matrix [[0,-1],[1,0]] (i^2 = -I), not a complex number.
    static {
        // Vectors
        memory.put("A", new Vector(List.of(1.0, 2.0, 3.0)));
        memory.put("B", new Vector(List.of(2.0, -1.0, 3.0)));
        memory.put("C", new Vector(List.of(-1.0, 2.0)));
        memory.put("D", new Vector(List.of(0.0, 1.0)));

        // Matrices
        memory.put("M1", Matrix.build(List.of(
                new Vector(List.of(1.0, 2.0, 6.0)),
                new Vector(List.of(3.0, -2.0, 3.0)),
                new Vector(List.of(0.0, -1.0, 5.0))
        )));

        memory.put("M2", Matrix.build(List.of(
                new Vector(List.of(6.0, 7.0, 6.0)),
                new Vector(List.of(4.0, -8.0, 6.0)),
                new Vector(List.of(0.0, -1.0, 9.0))
        )));


        memory.put("I", Matrix.build(List.of(
                new Vector(List.of(1.0, 0.0, 0.0)),
                new Vector(List.of(0.0, 1.0, 0.0)),
                new Vector(List.of(0.0, 0.0, 1.0))
        )));

        memory.put("i", Matrix.build(List.of(
                new Vector(List.of(0.0, -1.0)),
                new Vector(List.of(1.0, 0.0))
        )));

        memory.put("M3", Matrix.build(List.of(
                new Vector(List.of(1.0, 2.0, 3.0, 4.0)),
                new Vector(List.of(1.0, 2.0, 3.0, 4.0)),
                new Vector(List.of(1.0, 2.0, 3.0, 4.0))
        )));



        memory.put("pi", Math.PI);
        memory.put("e", Math.E);
    }

    /**
     * Step 1. Finds the declarations (NAME = Matrix(n, m) / NAME = Vector(n)).
     * Nothing else is evaluated.
     *
     * New names are added to memory as empty (zero) values; existing ones are kept.
     *
     * @return NAME -> empty Matrix/Vector of the declared size, in command order
     *         (tells the UI which inputs to show)
     * @throws CommandException bad declaration, constant name, or name declared twice
     */
    public static Map<String, Object> prepareVariables(List<String> commands) {
        Map<String, Object> templates = new LinkedHashMap<>();

        // 1st pass: validate everything before touching memory
        for (String command : commands) {
            Node declaration = parseIfConstructor(command);
            if (declaration == null) {
                continue;
            }

            String name = declaration.text();
            if (CONSTANTS.contains(name)) {
                throw new CommandException(name + " is a constant and cannot be reassigned");
            }
            if (templates.containsKey(name)) {
                throw new CommandException(name + " is declared more than once");
            }

            templates.put(name, Evaluator.buildEmpty(declaration));
        }

        // 2nd pass: make sure every declared name exists as a key in memory
        for (Map.Entry<String, Object> entry : templates.entrySet()) {
            if (!memory.containsKey(entry.getKey())) {
                memory.put(entry.getKey(), entry.getValue());
            }
        }

        return templates;
    }

    /**
     * Runs the commands with no manual values.
     *
     * @return one result per command, in order
     * @throws CommandException if any command is a declaration (its values would be empty)
     */
    public static List<CompiledResult> interprete(List<String> commands) {
        List<Node> trees = parseAll(commands);

        for (int i = 0; i < trees.size(); i++) {
            if (trees.get(i).kind() == NodeKind.DECLARE) {
                throw new CommandException("'" + commands.get(i).trim()
                        + "' declares a " + trees.get(i).children().get(0).text()
                        + ": its values must be filled in by hand before compiling");
            }
        }

        return run(commands, trees);
    }

    /**
     * Step 2. Loads manualValues into memory, then runs the commands.
     * Every declared Matrix/Vector needs a value of the declared size.
     * If manualValues is null, same as compile(commands).
     *
     * @return one result per command, in order
     * @throws CommandException missing/wrong-size value, or any error while running
     */
    public static List<CompiledResult> interprete(List<String> commands, Map<String, Object> manualValues) {
        if (manualValues == null) {
            return interprete(commands);
        }

        List<Node> trees = parseAll(commands);

        // Check every declaration got a value of the right size before touching memory
        for (Node tree : trees) {
            if (tree.kind() != NodeKind.DECLARE) {
                continue;
            }

            Object value = manualValues.get(tree.text());
            if (value == null) {
                throw new CommandException("Missing the values of " + tree.text());
            }
            if (!Evaluator.sameShape(Evaluator.buildEmpty(tree), value)) {
                throw new CommandException(tree.text() + " must be a " + Evaluator.describe(tree));
            }
        }

        for (Map.Entry<String, Object> entry : manualValues.entrySet()) {
            assignToMemory(entry.getKey(), freshCopy(entry.getValue()));
        }

        return run(commands, trees);
    }

    /** Deep copy, so later changes in memory don't touch the caller's objects. */
    private static Object freshCopy(Object value) {
        if (value instanceof Matrix m) {
            List<Vector> rows = new ArrayList<>();
            for (Vector row : m.getRows()) {
                rows.add(new Vector(row));
            }
            return Matrix.build(rows);
        }

        if (value instanceof Vector v) {
            return new Vector(v);
        }

        return value;
    }

    /**
     * Evaluates one expression that must give a finite number
     * (e.g. a matrix cell typed as "sqrt(2)/2").
     *
     * @throws CommandException for assignments, non-numbers, NaN or infinity
     */
    public static double evaluateEntry(String entry) {
        Node tree = new Parser(new Tokenizer(entry).getTokens()).parse();

        if (tree.kind() == NodeKind.ASSIGN || tree.kind() == NodeKind.DECLARE) {
            throw new CommandException("'" + entry.trim() + "' is a declaration, only values are allowed here");
        }

        if (Evaluator.evaluateNode(tree) instanceof Double d) {
            if (d.isNaN() || d.isInfinite()) {
                throw new CommandException("'" + entry.trim() + "' does not give a valid number");
            }
            return d;
        }

        throw new CommandException("'" + entry.trim() + "' is not a number");
    }

    /** Evaluates the trees in order. A DECLARE returns the value already in memory. */
    private static List<CompiledResult> run(List<String> commands, List<Node> trees) {
        List<CompiledResult> results = new ArrayList<>();

        for (int i = 0; i < trees.size(); i++) {
            Node tree = trees.get(i);
            Object result = tree.kind() == NodeKind.DECLARE
                    ? memory.get(tree.text())
                    : Evaluator.evaluateNode(tree);

            results.add(new CompiledResult(commands.get(i), result));
        }

        return results;
    }

    /** Parses everything first, so a syntax error stops the run before anything executes. */
    private static List<Node> parseAll(List<String> commands) {
        List<Node> trees = new ArrayList<>();
        for (String command : commands) {
            trees.add(new Parser(new Tokenizer(command).getTokens()).parse());
        }
        return trees;
    }

    /** Parses the command only if it uses Matrix/Vector; otherwise null. */
    private static Node parseIfConstructor(String command) {
        List<Token> tokens = new Tokenizer(command).getTokens();

        boolean hasConstructor = false;
        for (Token t : tokens) {
            if (t.tokenType == TokenType.CONSTRUCTOR) {
                hasConstructor = true;
                break;
            }
        }

        return hasConstructor ? new Parser(tokens).parse() : null;
    }

    /**
     * Saves a variable.
     *
     * @throws CommandException if the name is a constant
     */
    public static void assignToMemory(String name, Object obj) {
        if (CONSTANTS.contains(name)) {
            throw new CommandException(name + " is a constant and cannot be reassigned");
        }

        memory.put(name, obj);
    }

    /** The live memory map (not a copy). */
    public static HashMap<String, Object> getMemory() {
        return memory;
    }
}
