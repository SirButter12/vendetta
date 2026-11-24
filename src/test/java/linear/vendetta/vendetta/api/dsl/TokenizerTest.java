package linear.vendetta.vendetta.api.dsl;

import static linear.vendetta.vendetta.api.dsl.Token.TokenType.COMMA;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.CONSTRUCTOR;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.END;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.EQUALS;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.FUNCT;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.LBRACKET;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.LPAREN;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.MINUS;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.NAME;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.NUMBER;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.PLUS;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.RBRACKET;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.RPAREN;
import static linear.vendetta.vendetta.api.dsl.Token.TokenType.STAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import linear.vendetta.vendetta.api.dsl.Token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenizerTest {

    private static List<TokenType> types(String command) {
        List<TokenType> result = new ArrayList<>();
        for (Token t : new Tokenizer(command).getTokens()) {
            result.add(t.tokenType);
        }
        return result;
    }

    @Test
    @DisplayName("A + B -> NAME PLUS NAME END")
    void simpleExpression() {
        assertEquals(List.of(NAME, PLUS, NAME, END), types("A + B"));
    }

    @Test
    @DisplayName("2.5 * A -> NUMBER STAR NAME END")
    void decimalNumber() {
        assertEquals(List.of(NUMBER, STAR, NAME, END), types("2.5 * A"));
    }

    @Test
    @DisplayName("Spaces do not matter")
    void whitespaceIsIgnored() {
        assertEquals(types("A+B"), types("  A   +   B  "));
    }

    @Test
    @DisplayName("Function names become FUNCT")
    void functionCall() {
        assertEquals(List.of(FUNCT, LPAREN, NAME, RPAREN, END), types("det(A)"));
    }

    @Test
    @DisplayName("Matrix(...) and Vector(...) become CONSTRUCTOR")
    void declaration() {
        assertEquals(List.of(NAME, EQUALS, CONSTRUCTOR, LPAREN, NUMBER, COMMA, NUMBER, RPAREN, END),
                types("M = Matrix(2, 3)"));
    }

    @Test
    void listLiteral() {
        assertEquals(List.of(LBRACKET, NUMBER, COMMA, NUMBER, RBRACKET, END), types("[1, 2]"));
    }

    @Test
    @DisplayName("1e-3 is one number, but 2e - 3 is not")
    void scientificNotation() {
        assertEquals(List.of(NUMBER, END), types("1e-3"));
        assertEquals(List.of(NUMBER, MINUS, NUMBER, END), types("5 - 3"));
    }

    @Test
    @DisplayName("The original text of each token is kept")
    void originalText() {
        List<Token> tokens = new Tokenizer("M2 = 3.75").getTokens();
        assertEquals("M2", tokens.get(0).oriText);
        assertEquals("3.75", tokens.get(2).oriText);
    }

    @Test
    @DisplayName("A # B is rejected")
    void unknownCharacter() {
        assertThrows(CommandException.class, () -> new Tokenizer("A # B"));
    }

    @Test
    @DisplayName(".5 and 2x are rejected")
    void invalidWords() {
        assertThrows(CommandException.class, () -> new Tokenizer(".5"));
        assertThrows(CommandException.class, () -> new Tokenizer("2x"));
    }
}
