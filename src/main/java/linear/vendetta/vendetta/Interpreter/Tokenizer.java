package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import linear.vendetta.vendetta.Interpreter.Token.TokenType;

/**
 * Splits a command into Tokens.
 *
 * Symbols (+ - * / ^ ( ) [ ] , =) and spaces end the current word; each word
 * becomes FUNCT, CONSTRUCTOR, NAME or NUMBER. The list always ends with END.
 * <pre>
 * "x = det(M1)*2" -> NAME EQUALS FUNCT LPAREN NAME RPAREN STAR NUMBER END
 * </pre>
 *
 * @author ian
 */
public class Tokenizer {
    private String command;
    private List<Token> tokens = new ArrayList<>();
    // Reserved names for built-in functions (must match Evaluator.call).
    private static final List<String> functions = List.of(
            //Matrix functions
            "adj","inv","trans","det", "triangulize",

            //Vector
            "dot", "cross", "project",

            //System of equations
            "solveSystem", "gaussJordan",

            //Math
            "sin", "cos", "tan", "sec", "csc", "cot",
            "arcsin", "arccos", "arctan", "arcsec", "arccsc", "arccot",

            "ln", "sqrt", "log", "log10", "mod"
    );

    // Reserved names that declare a variable to be filled in by hand.
    private static final List<String> constructors = List.of(
            "Matrix", "Vector"
    );
    // Single-character symbols -> token type.
    private static HashMap<Character, TokenType> tokenMapping = new HashMap();


    static {
        tokenMapping.put(',', TokenType.COMMA);
        tokenMapping.put('=', TokenType.EQUALS);
        tokenMapping.put('(', TokenType.LPAREN);
        tokenMapping.put('-', TokenType.MINUS);
        tokenMapping.put('+', TokenType.PLUS);
        tokenMapping.put(')', TokenType.RPAREN);
        tokenMapping.put('/', TokenType.SLASH);
        tokenMapping.put('*', TokenType.STAR);
        tokenMapping.put('^', TokenType.EXP);
        tokenMapping.put('[', TokenType.LBRACKET);
        tokenMapping.put(']', TokenType.RBRACKET);
    }

    /**
     * Tokenizes right away; read the result with getTokens().
     *
     * @throws CommandException if a word is not a valid name or number
     */
    public Tokenizer(String command) {
        this.command = command;

        tokenize();
    }

    /**
     * Walks the text char by char, collecting a word in `current`
     * until a symbol or a space ends it.
     */
    private void tokenize() {
        StringBuilder current = new StringBuilder();
        char[] commandChars = command.toCharArray();

        for (int i = 0; i < command.length(); i++) {
            char c = commandChars[i];

            if (tokenMapping.containsKey(c)) {
                validate(current.toString());
                tokens.add(new Token(String.valueOf(c), tokenMapping.get(c)));
                clear(current);
                continue;
            }

            if (Character.isWhitespace(c)) {
                validate(current.toString());
                clear(current);
                continue;
            }

            current.append(commandChars[i]);
        }

        validate(current.toString());
        tokens.add(new Token("", Token.TokenType.END));
    }

    /**
     * Classifies a finished word and adds its token. Empty words are ignored.
     * Numbers: digits with optional decimals ("2", "2.5"; not ".5" or "1e3").
     * Names: letter followed by letters/digits.
     *
     * @throws CommandException (no message) for anything else, e.g. "2x"
     */
    private void validate(String str) {
        if (str.isEmpty()) {
            return;
        }

        if (functions.contains(str)) {
            tokens.add(new Token(str, Token.TokenType.FUNCT));
        } else if(constructors.contains(str)) {
            tokens.add(new Token(str, Token.TokenType.CONSTRUCTOR));
        } else if (str.matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
            tokens.add(new Token(str, Token.TokenType.NAME));
        } else if (str.matches("^\\d+(\\.\\d+)?$")) {
            tokens.add(new Token(str, Token.TokenType.NUMBER));
        } else {
            throw new CommandException();
        }
    }

    /** Empties the word buffer. */
    private void clear(StringBuilder str) {
        str.setLength(0);
    }

    /** The tokens, ending with END. */
    public List<Token> getTokens() {
        return tokens;
    }
}
