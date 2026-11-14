package linear.vendetta.vendetta.Interpreter;

import java.util.List;

/**
 * Immutable syntax tree node built by the Parser.
 *
 * kind = what it is, text = its value/name/operator, children = operands or arguments.
 *
 * @author ian
 */

public record Node(NodeKind kind, String text, List<Node> children) {

    /**
     * <pre>
     * NUMBER   "2.5"              text = the number
     * NAME     "M1"               text = variable name
     * CALL     "det(M1)"          text = function, children = arguments
     * BINARY   "A + B"            text = operator (+ - * / ^), 2 children
     * ASSIGN   "x = A + B"        text = variable name, 1 child (the value)
     * DECLARE  "M = Matrix(2, 3)" text = variable name, 1 child: CALL to Matrix/Vector with the sizes
     * LIST     "[1, 2]"           children = elements (nested LIST = matrix rows)
     * </pre>
     */
    public enum NodeKind {
        NUMBER, NAME, CALL, BINARY, ASSIGN, DECLARE, LIST
    }

    /** Copies children so the node cannot change later. */
    public Node {
        children = List.copyOf(children);
    }
}

