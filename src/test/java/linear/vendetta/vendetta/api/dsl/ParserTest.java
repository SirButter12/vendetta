package linear.vendetta.vendetta.api.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import linear.vendetta.vendetta.api.dsl.Node.NodeKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParserTest {

    private static Node parse(String command) {
        return new Parser(new Tokenizer(command).getTokens()).parse();
    }

    @Test
    @DisplayName("* binds tighter than +: 1 + 2 * 3 = 1 + (2 * 3)")
    void precedence() {
        Node root = parse("1 + 2 * 3");
        assertEquals(NodeKind.BINARY, root.kind());
        assertEquals("+", root.text());
        assertEquals("*", root.children().get(1).text());
    }

    @Test
    @DisplayName("- is left-associative: 8 - 3 - 2 = (8 - 3) - 2")
    void leftAssociative() {
        Node root = parse("8 - 3 - 2");
        assertEquals("-", root.text());
        assertEquals("-", root.children().get(0).text());
        assertEquals("2", root.children().get(1).text());
    }

    @Test
    @DisplayName("^ is right-associative: 2 ^ 3 ^ 2 = 2 ^ (3 ^ 2)")
    void rightAssociativePower() {
        Node root = parse("2 ^ 3 ^ 2");
        assertEquals("^", root.text());
        assertEquals("2", root.children().get(0).text());
        assertEquals("^", root.children().get(1).text());
    }

    @Test
    @DisplayName("Parentheses override precedence: (1 + 2) * 3")
    void parentheses() {
        Node root = parse("(1 + 2) * 3");
        assertEquals("*", root.text());
        assertEquals("+", root.children().get(0).text());
    }

    @Test
    @DisplayName("Nested calls: det(A + B)")
    void nestedCall() {
        Node root = parse("det(A + B)");
        assertEquals(NodeKind.CALL, root.kind());
        assertEquals("det", root.text());
        assertEquals(NodeKind.BINARY, root.children().get(0).kind());
    }

    @Test
    void assignment() {
        Node root = parse("x = 2 + 3");
        assertEquals(NodeKind.ASSIGN, root.kind());
        assertEquals("x", root.text());
        assertEquals(1, root.children().size());
    }

    @Test
    void declaration() {
        Node root = parse("M = Matrix(2, 3)");
        assertEquals(NodeKind.DECLARE, root.kind());
        assertEquals("M", root.text());
    }

    @Test
    void listLiteral() {
        Node root = parse("[[1, 2], [3, 4]]");
        assertEquals(NodeKind.LIST, root.kind());
        assertEquals(2, root.children().size());
        assertEquals(NodeKind.LIST, root.children().get(0).kind());
    }

    @Test
    @DisplayName("Malformed commands are rejected")
    void malformedInput() {
        assertThrows(CommandException.class, () -> parse("det(A"));          // missing )
        assertThrows(CommandException.class, () -> parse("A +"));            // missing operand
        assertThrows(CommandException.class, () -> parse("dot(A, , B)"));    // stray comma
        assertThrows(CommandException.class, () -> parse("det(M) det(M)"));  // leftover tokens
    }

    @Test
    @DisplayName("Matrix(...) must be the whole command, with plain numbers as sizes")
    void declarationRules() {
        assertThrows(CommandException.class, () -> parse("A + Matrix(2, 2)"));
        assertThrows(CommandException.class, () -> parse("M = Matrix(1 + 1, 2)"));
    }
}
