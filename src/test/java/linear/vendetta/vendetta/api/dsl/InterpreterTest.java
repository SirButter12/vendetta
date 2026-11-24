package linear.vendetta.vendetta.api.dsl;

import static linear.vendetta.vendetta.api.TestUtils.EPS;
import static linear.vendetta.vendetta.api.TestUtils.assertMatrixClose;
import static linear.vendetta.vendetta.api.TestUtils.assertVectorClose;
import static linear.vendetta.vendetta.api.TestUtils.mat;
import static linear.vendetta.vendetta.api.TestUtils.row;
import static linear.vendetta.vendetta.api.TestUtils.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.linear_algebra.MultipleOrNoSolutionException;
import linear.vendetta.vendetta.api.linear_algebra.SolutionVector;
import linear.vendetta.vendetta.api.linear_algebra.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * End-to-end tests: command text in, value out.
 * The interpreter memory is static, so every test starts from a clean memory.
 */
class InterpreterTest {

    @BeforeEach
    void cleanMemory() {
        Interpreter.resetMemory();
    }

    /** Runs the commands in order and returns the result of the last one. */
    private static Object run(String... commands) {
        List<CompiledResult> results = Interpreter.interprete(List.of(commands));
        return results.get(results.size() - 1).getResult();
    }

    private static double number(String... commands) {
        return (Double) run(commands);
    }

    // ---------- Evaluation order ----------

    @Test
    @DisplayName("Multiplication before addition, parentheses first")
    void precedence() {
        assertEquals(14.0, number("2 + 3 * 4"), EPS);
        assertEquals(20.0, number("(2 + 3) * 4"), EPS);
        assertEquals(3.0, number("8 - 3 - 2"), EPS);
        assertEquals(512.0, number("2 ^ 3 ^ 2"), EPS);
        assertEquals(-4.0, number("-2 ^ 2"), EPS);
    }

    @Test
    @DisplayName("Det(A) * Inv(A - B) computes A - B, then Inv, then Det, then the product")
    void nestedExampleFromThePlan() {
        // det(A) = 1 and A - B = [[1, 1], [1, 2]], whose inverse is [[2, -1], [-1, 1]]
        Object r = run("A = [[2, 1], [1, 1]]", "B = [[1, 0], [0, -1]]", "det(A) * inv(A - B)");
        assertMatrixClose(mat(row(2, -1), row(-1, 1)), (Matrix) r);
    }

    // ---------- Memory ----------

    @Test
    @DisplayName("name = expression stores the result")
    void assignmentStoresTheValue() {
        run("x = 2 + 3");
        assertEquals(5.0, (Double) Interpreter.getMemory().get("x"), EPS);
        assertEquals(10.0, number("x * 2"), EPS);
    }

    @Test
    @DisplayName("An undeclared name is an error")
    void undeclaredName() {
        assertThrows(CommandException.class, () -> run("Z + 1"));
    }

    @Test
    @DisplayName("pi and e exist and cannot be reassigned")
    void constants() {
        assertEquals(Math.PI, number("pi"), EPS);
        assertThrows(CommandException.class, () -> run("pi = 3"));
    }

    @Test
    @DisplayName("resetMemory leaves only pi and e")
    void resetMemory() {
        run("x = 5");
        Interpreter.resetMemory();
        assertEquals(2, Interpreter.getMemory().size());
        assertFalse(Interpreter.getMemory().containsKey("x"));
    }

    @Test
    @DisplayName("Operations never modify the variables they use")
    void originalsAreNotModified() {
        run("A = [[1, 2], [3, 4]]", "B = A + A", "C = 3 * A", "D = inv(A)", "E = trans(A)");
        assertEquals(mat(row(1, 2), row(3, 4)), Interpreter.getMemory().get("A"));
    }

    // ---------- Functions ----------

    @Test
    void matrixFunctions() {
        run("A = [[1, 2], [3, 4]]");
        assertEquals(-2.0, number("det(A)"), EPS);
        assertEquals(mat(row(4, -2), row(-3, 1)), run("adj(A)"));
        assertEquals(mat(row(1, 3), row(2, 4)), run("trans(A)"));
        assertMatrixClose(Matrix.identity(2), (Matrix) run("inv(A) * A"));
    }

    @Test
    void vectorFunctions() {
        run("u = [1, 0, 0]", "v = [0, 1, 0]");
        assertEquals(vec(0, 0, 1), run("cross(u, v)"));
        assertEquals(0.0, number("dot(u, v)"), EPS);
        assertVectorClose(vec(3, 0), (Vector) run("project([3, 4], [1, 0])"));
    }

    @Test
    @DisplayName("angleInBet accepts vectors of different dimensions")
    void angleInBetCommand() {
        assertEquals(Math.PI / 2, number("angleInBet([1, 0], [0, 1, 0])"), EPS);
        assertEquals(number("angleInBet([1, 2, 0], [1, 2, 3])"), number("angleInBet([1, 2], [1, 2, 3])"), EPS);
    }

    @Test
    @DisplayName("dot works for vector.vector, matrix.matrix and matrix.vector")
    void dotProduct() {
        run("A = [[1, 2], [3, 4]]", "b = [5, 6]");
        assertEquals(61.0, number("dot(b, b)"), EPS);
        assertEquals(mat(row(7, 10), row(15, 22)), run("dot(A, A)"));
        assertEquals(vec(17, 39), run("dot(A, b)"));
    }

    @Test
    @DisplayName("gaussJordan returns the solution with its steps")
    void gaussJordan() {
        Object r = run("A = [[1, 2], [3, 4]]", "b = [5, 6]", "S = gaussJordan(A, b)");
        SolutionVector s = assertInstanceOf(SolutionVector.class, r);
        assertVectorClose(vec(-4, 4.5), s);
        assertFalse(s.getSteps().isEmpty());
    }

    @Test
    void solveSystem() {
        Object r = run("A = [[2, 1], [1, 3]]", "b = [3, 5]", "solveSystem(A, b)");
        assertVectorClose(vec(0.8, 1.4), (Vector) r);
    }

    // ---------- Errors ----------

    @Test
    @DisplayName("Adding a matrix and a vector is rejected")
    void typeMismatch() {
        run("A = [[1, 2], [3, 4]]", "b = [5, 6]");
        assertThrows(CommandException.class, () -> run("A + b"));
    }

    @Test
    @DisplayName("Adding matrices of different sizes is rejected")
    void sizeMismatch() {
        run("A = [[1, 2], [3, 4]]", "B = [[1, 2, 3]]");
        assertThrows(IllegalArgumentException.class, () -> run("A + B"));
    }

    @Test
    void singularMatrix() {
        assertThrows(ArithmeticException.class, () -> run("inv([[1, 2], [2, 4]])"));
    }

    @Test
    void systemWithoutUniqueSolution() {
        assertThrows(MultipleOrNoSolutionException.class, () -> run("gaussJordan([[1, 2], [2, 4]], [5, 6])"));
    }

    @Test
    @DisplayName("Wrong number of arguments is rejected")
    void wrongArgumentCount() {
        assertThrows(CommandException.class, () -> run("det([[1, 2], [3, 4]], 2)"));
    }

    @Test
    @DisplayName("Dividing a matrix by zero is rejected (plan: 'the divisor is a non-zero number')")
    void divisionByZero() {
        run("A = [[1, 2], [3, 4]]");
        assertThrows(RuntimeException.class, () -> run("A / 0"));
    }

    @Test
    @DisplayName("A syntax error in a list stops the whole list before anything runs")
    void syntaxErrorStopsTheList() {
        assertThrows(CommandException.class, () -> Interpreter.interprete(List.of("x = 1", "y = (2")));
        assertFalse(Interpreter.getMemory().containsKey("x"));
    }

    // ---------- Declarations and manual values ----------

    @Test
    @DisplayName("prepareVariables returns zero-filled templates of the declared size")
    void prepareVariables() {
        Map<String, Object> t = Interpreter.prepareVariables(List.of("A = Matrix(2, 3)", "b = Vector(2)", "c = 5"));
        assertEquals(2, t.size());
        assertEquals(new Matrix(2, 3), t.get("A"));
        assertEquals(new Vector(2), t.get("b"));
    }

    @Test
    void duplicatedDeclaration() {
        assertThrows(CommandException.class,
                () -> Interpreter.prepareVariables(List.of("A = Matrix(2, 2)", "A = Vector(2)")));
    }

    @Test
    @DisplayName("Manual values are loaded and used by later commands")
    void manualValues() {
        Map<String, Object> manual = new HashMap<>();
        manual.put("A", mat(row(1, 2), row(3, 4)));
        List<CompiledResult> r = Interpreter.interprete(List.of("A = Matrix(2, 2)", "d = det(A)"), manual);
        assertEquals(-2.0, (Double) r.get(1).getResult(), EPS);
    }

    @Test
    @DisplayName("Manual values are copied: editing the map's matrix later does not change memory")
    void manualValuesAreCopied() {
        Matrix typed = mat(row(1, 2), row(3, 4));
        Map<String, Object> manual = new HashMap<>();
        manual.put("A", typed);
        Interpreter.interprete(List.of("A = Matrix(2, 2)"), manual);
        typed.set(0, 0, 99);
        assertEquals(1.0, ((Matrix) Interpreter.getMemory().get("A")).get(0, 0), EPS);
    }

    @Test
    void missingOrWrongManualValues() {
        Map<String, Object> manual = new HashMap<>();
        assertThrows(CommandException.class, () -> Interpreter.interprete(List.of("A = Matrix(2, 2)"), manual));
        manual.put("A", new Matrix(3, 3));
        assertThrows(CommandException.class, () -> Interpreter.interprete(List.of("A = Matrix(2, 2)"), manual));
    }

    @Test
    @DisplayName("A declaration cannot be run without its values")
    void declarationWithoutValues() {
        assertThrows(CommandException.class, () -> Interpreter.interprete(List.of("A = Matrix(2, 2)")));
    }

    // ---------- Cell values ----------

    @Test
    @DisplayName("Cells accept expressions that give a finite number")
    void evaluateEntry() {
        assertEquals(Math.sqrt(2) / 2, Interpreter.evaluateEntry("sqrt(2)/2"), EPS);
        assertEquals(-3.0, Interpreter.evaluateEntry("-3"), EPS);
    }

    @Test
    @DisplayName("Cells reject assignments, non-numbers and infinity")
    void evaluateEntryRejects() {
        run("b = [1, 2]");
        assertThrows(CommandException.class, () -> Interpreter.evaluateEntry("A = 3"));
        assertThrows(CommandException.class, () -> Interpreter.evaluateEntry("b"));
        assertThrows(CommandException.class, () -> Interpreter.evaluateEntry("1/0"));
        assertTrue(Interpreter.getMemory().containsKey("b"));
    }
}
