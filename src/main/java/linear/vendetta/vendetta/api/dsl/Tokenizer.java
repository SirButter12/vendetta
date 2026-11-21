package linear.vendetta.vendetta.api.dsl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import linear.vendetta.vendetta.api.dsl.Token.TokenType;

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
            "dot", "cross", "project", "length", "direction", "polarV", "polarize",

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
     * @throws CommandException for anything else, e.g. "2x", with a message
     *         that says what is wrong (see invalidWordMessage)
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
            throw new CommandException(invalidWordMessage(str));
        }
    }

    /**
     * Explains why a word is neither a valid name nor a valid number.
     * Checked from the most specific case to the most general one.
     */
    private static String invalidWordMessage(String str) {
        String start = "'" + str + "' is not valid: ";

        // A character that can never appear in a command: @ # _ % ! ; á ...
        for (char c : str.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && c != '.') {
                return start + "the character '" + c + "' is not allowed. "
                        + "Names can only have letters and digits, and the only symbols are + - * / ^ ( ) [ ] , =";
            }
            if (Character.isLetter(c) && !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))) {
                return start + "the letter '" + c + "' is not allowed. Use only letters without accents (a-z, A-Z)";
            }
        }

        // Scientific notation: 1e3, 2.5E10, and "1e" from 1e-3 (the '-' splits the word)
        if (str.matches("^\\d+(\\.\\d+)?[eE]\\d*$")) {
            return start + "scientific notation is not supported yet. Write the full number (1e3 -> 1000) "
                    + "or use a power (2.5 * 10^3)";
        }
        // A number glued to a name: 2x, 3pi, 2.5sqrt
        java.util.regex.Matcher glued = java.util.regex.Pattern
                .compile("^(\\d+(?:\\.\\d+)?)([a-zA-Z][a-zA-Z0-9]*)$").matcher(str);
        if (glued.matches()) {
            return start + "names can't start with a digit. To multiply, write "
                    + glued.group(1) + " * " + glued.group(2);
        }
        if (str.startsWith(".")) {
            return start + "numbers must have a digit before the decimal point. Write 0" + str;
        }
        if (str.endsWith(".")) {
            return start + "numbers must have a digit after the decimal point. Write "
                    + str.substring(0, str.length() - 1) + " or " + str + "0";
        }
        if (str.indexOf('.') != str.lastIndexOf('.')) {
            return start + "a number can only have one decimal point";
        }
        if (str.contains(".")) {
            return start + "the decimal point can only be used in numbers (2.5), not in names";
        }
        if (Character.isDigit(str.charAt(0))) {
            return start + "names can't start with a digit";
        }
        return start + "it is not a valid name or number";
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
