/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import linear.vendetta.vendetta.API.Matrix;
import linear.vendetta.vendetta.API.Vector;
import linear.vendetta.vendetta.Interpreter.Node.NodeKind;
import linear.vendetta.vendetta.Interpreter.Token.TokenType;

/**
 *
 * @author ian
 */
public class Interpreter {
    private static HashMap<String, Object> memory = new HashMap();
    private static final Set<String> CONSTANTS = Set.of("pi", "e", "i");
    
    static {
        // Vectores
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
     * Enter: looks for the constructors ( NAME = Matrix(n, m) / NAME = Vector(n) )
     * among the commands. Nothing is evaluated.
     *
     * Returns, in order, NAME -> empty Matrix/Vector with the requested size,
     * so the frontend knows which inputs to show and with which dimensions.
     * If NAME is not a key in memory yet it is created with that empty value;
     * if it already is, it is left alone (compile will overwrite it with the
     * manual values anyway). Memory is never reset.
     *
     * A constructor without the right structure throws CommandException.
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
     * Compile without manual values. If any command is a constructor it throws:
     * those variables would only hold placeholder (garbage) values.
     * Otherwise it works exactly as before.
     */
    public static List<CompiledResult> compile(List<String> commands) {
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

    public static List<CompiledResult> compile(List<String> commands, Map<String, Object> manualValues) {
        if (manualValues == null) {
            return compile(commands);
        }

        List<Node> trees = parseAll(commands);

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

    private static List<Node> parseAll(List<String> commands) {
        List<Node> trees = new ArrayList<>();
        for (String command : commands) {
            trees.add(new Parser(new Tokenizer(command).getTokens()).parse());
        }
        return trees;
    }

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

    public static void assignToMemory(String name, Object obj) {
        if (CONSTANTS.contains(name)) {
            throw new CommandException(name + " is a constant and cannot be reassigned");
        }
        
        memory.put(name, obj);
    }
    
    public static HashMap<String, Object> getMemory() {
        return memory;
    }    
}
