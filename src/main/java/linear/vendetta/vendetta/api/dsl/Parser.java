package linear.vendetta.vendetta.api.dsl;

import java.util.ArrayList;
import java.util.List;
import linear.vendetta.vendetta.api.dsl.Node.NodeKind;
import linear.vendetta.vendetta.api.dsl.Token.TokenType;

/**
 * Recursive-descent parser: tokens -> Node tree.
 * One method per grammar rule, lowest to highest precedence:
 * <pre>
 * statement   = NAME '=' expression | expression
 * expression  = term (('+' | '-') term)*            left-assoc
 * term        = unary (('*' | '/') unary)*          left-assoc
 * unary       = '-' unary | exp                     -2^2 = -(2^2) = -4
 * exp         = factor ('^' unary)?                 right-assoc: 2^3^2 = 2^9
 * factor      = NUMBER | NAME | '(' expression ')'
 *             | FUNCT '(' [expression (',' expression)*] ')'
 *             | '[' expression (',' expression)* ']'
 * constructor = NAME '=' (Matrix | Vector) '(' NUMBER (',' NUMBER)* ')'   whole command only
 * </pre>
 *
 * @author ian
 */
public class Parser {
    private final List<Token> tokens;
    private int pos = 0;    // index of the current token
    // Required start of a constructor command: NAME = Matrix/Vector (
    private static final List<TokenType> constStructure = List.of(
            Token.TokenType.NAME,
            Token.TokenType.EQUALS,
            Token.TokenType.CONSTRUCTOR,
            Token.TokenType.LPAREN
            );

    /** @param tokens output of Tokenizer (must end with END) */
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /** True if the command starts with NAME = CONSTRUCTOR (. */
    private boolean matchesConstTokens() {
        if (tokens.size() < constStructure.size()) {    // "Matrix" alone has only 2 tokens
            return false;
        }

        for (int i = 0; i < constStructure.size(); i ++) {
            if (tokens.get(i).tokenType != constStructure.get(i)) {
                return false;
            }
        }

        return true;
    }

    /** True if Matrix/Vector appears anywhere in the command. */
    private boolean containsConst() {
        for (Token t: tokens) {
            if (t.tokenType == Token.TokenType.CONSTRUCTOR) {
                return true;
            }
        }

        return false;
    }

    /** Current token, not consumed. */
    private Token peek() {
        return tokens.get(pos);
    }

    /** Consumes and returns the current token. Never moves past END. */
    private Token next() {
        Token t = tokens.get(pos);
        if (t.tokenType != TokenType.END) {
            pos++;
        }
        return t;
    }

    /** Consumes a token of the given type, or throws CommandException(message). */
    private Token expect(TokenType type, String message) {
        if (peek().tokenType != type) {
            throw new CommandException(message);
        }
        return next();
    }

    /**
     * Parses the whole command (entry point).
     *
     * @throws CommandException on a syntax error or leftover tokens
     */
    public Node parse() {
        if (containsConst()) {
            if (!matchesConstTokens()) {
                throw new CommandException("Matrix and Vector can only be used as  NAME = Matrix(n, m)  or  NAME = Vector(n)");
            }
            return parseConstructor();
        }

        Node tree = parseStatement();
        if (peek().tokenType != TokenType.END) {
            throw new CommandException("Unexpected '" + peek().oriText + "'");
        }
        return tree;
    }


    /**
     * NAME = Matrix(2, 3) -> DECLARE(NAME) with one child CALL(Matrix) holding the sizes.
     * Sizes must be plain numbers (no expressions).
     */
    private Node parseConstructor() {
        String name = next().oriText;        // NAME
        next();                              // =
        String constructor = next().oriText; // Matrix / Vector
        next();                              // (

        String sizeError = "Sizes must be written as plain numbers, e.g. Matrix(2, 3)";
        List<Node> sizes = new ArrayList<>();
        sizes.add(new Node(NodeKind.NUMBER, expect(TokenType.NUMBER, sizeError).oriText, List.of()));

        while (peek().tokenType == TokenType.COMMA) {
            next();
            sizes.add(new Node(NodeKind.NUMBER, expect(TokenType.NUMBER, sizeError).oriText, List.of()));
        }

        if (peek().tokenType == TokenType.END) {
            throw new CommandException("Missing ')'");
        }
        expect(TokenType.RPAREN, sizeError);    // e.g. Matrix(1+1, 2)

        if (peek().tokenType != TokenType.END) {
            throw new CommandException("Unexpected '" + peek().oriText + "' after the constructor");
        }

        Node call = new Node(NodeKind.CALL, constructor, sizes);
        return new Node(NodeKind.DECLARE, name, List.of(call));
    }

    /** Smallest unit: number, variable, (expr), function call or [list]. */
    private Node parseFactor() {
        Token t = next();

        if (t.tokenType == TokenType.NUMBER) {
            return new Node(NodeKind.NUMBER, t.oriText, List.of());
        }
        if (t.tokenType == TokenType.NAME) {
            return new Node(NodeKind.NAME, t.oriText, List.of());
        }
        if (t.tokenType == TokenType.LPAREN) {
            Node inner = parseExpression();
            expect(TokenType.RPAREN, "Missing ')'");
            return inner;
        }

        if (t.tokenType == TokenType.FUNCT) {
            expect(Token.TokenType.LPAREN, "Expected '(' after " + t.oriText);
            ArrayList<Node> arguments = new ArrayList<>();

            if (peek().tokenType != TokenType.RPAREN) {
                arguments.add(parseExpression());
            }

            while (peek().tokenType == TokenType.COMMA) {
                next();
                arguments.add(parseExpression());
            }

            expect(TokenType.RPAREN, "Missing ')'");

            return new Node(NodeKind.CALL, t.oriText, arguments);
        }

        if (t.tokenType == TokenType.LBRACKET) {
            return parseList();
        }

        if (t.tokenType == TokenType.END) {
            throw new CommandException("Unexpected end of command");
        }

        throw new CommandException("Unexpected '" + t.oriText + "'");
    }

    /**
     * Called after '['. Elements are expressions: [1, 2+3] -> LIST.
     * Nested lists ([[1,2],[3,4]]) become matrix rows in the Evaluator.
     */
    private Node parseList() {
        List<Node> elements = new ArrayList<>();

        if (peek().tokenType == TokenType.RBRACKET) {
            throw new CommandException("Empty brackets: a vector needs at least one value");
        }

        elements.add(parseExpression());
        while (peek().tokenType == TokenType.COMMA) {
            next();
            elements.add(parseExpression());
        }

        expect(TokenType.RBRACKET, "Missing ']'");
        return new Node(NodeKind.LIST, "[]", elements);
    }

    /**
     * Leading minus. -NUMBER is folded into a negative NUMBER;
     * anything else becomes (-1 * x).
     */
    private Node parseUnary() {
        if (peek().tokenType == TokenType.MINUS) {
            next();
            Node operand = parseUnary();
            if (operand.kind() == NodeKind.NUMBER) {
                return new Node(NodeKind.NUMBER, String.valueOf(-Double.parseDouble(operand.text())), List.of());
            }
            return new Node(NodeKind.BINARY, "*", List.of(new Node(NodeKind.NUMBER, "-1", List.of()), operand));
        }
        return parseExp();
    }

    /** Addition and subtraction. */
    private Node parseExpression() {
        Node left = parseTerm();
        while (peek().tokenType == TokenType.PLUS || peek().tokenType == TokenType.MINUS) {
            String op = next().oriText;
            Node right = parseTerm();
            left = new Node(NodeKind.BINARY, op, List.of(left, right));
        }
        return left;
    }

    /** Multiplication and division. */
    private Node parseTerm() {
        Node left = parseUnary();
        while (peek().tokenType == TokenType.STAR || peek().tokenType == TokenType.SLASH) {
            String op = next().oriText;
            Node right = parseUnary();
            left = new Node(NodeKind.BINARY, op, List.of(left, right));
        }
        return left;
    }

    /** Power. The exponent is parsed as unary, so 2^-1 works. */
    private Node parseExp() {
        Node base = parseFactor();
        if (peek().tokenType == TokenType.EXP) {
            String op = next().oriText;
            Node exponent = parseUnary();
            return new Node(NodeKind.BINARY, op, List.of(base, exponent));
        }
        return base;
    }

    /** Assignment if it starts with NAME =, otherwise a plain expression. */
    private Node parseStatement() {
        if ((pos + 1 < tokens.size()) && tokens.get(pos).tokenType.equals(TokenType.NAME) && tokens.get(pos + 1).tokenType.equals(TokenType.EQUALS)) {
            String name = next().oriText;
            next();
            Node valor = parseExpression();

            return new Node(NodeKind.ASSIGN, name, List.of(valor));
        }

        return parseExpression();
    }
}
