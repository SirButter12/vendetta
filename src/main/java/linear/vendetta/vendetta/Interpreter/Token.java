package linear.vendetta.vendetta.Interpreter;

/**
 * One piece of a command: its original text and its type.
 * e.g. "det" -> FUNCT, "M1" -> NAME, "2.5" -> NUMBER, "+" -> PLUS.
 *
 * @author ian
 */
public class Token {
    public String oriText;
    public TokenType tokenType;

    public Token(String oriText, TokenType tokenType) {
        this.oriText = oriText;
        this.tokenType = tokenType;
    }

    /**
     * NAME = variable, FUNCT = built-in function, CONSTRUCTOR = Matrix/Vector,
     * EXP = ^, LBRACKET/RBRACKET = [ ], END = end-of-command marker.
     */
    public static enum TokenType {
        NAME, NUMBER, PLUS, MINUS, STAR, SLASH, EXP, LPAREN, RPAREN, COMMA, EQUALS, LBRACKET, RBRACKET, FUNCT, CONSTRUCTOR, END
    }
}
