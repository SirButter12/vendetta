package linear.vendetta.vendetta.api.linear_algebra;

import static linear.vendetta.vendetta.api.TestUtils.assertVectorClose;
import static linear.vendetta.vendetta.api.TestUtils.mat;
import static linear.vendetta.vendetta.api.TestUtils.row;
import static linear.vendetta.vendetta.api.TestUtils.vec;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SystemMatrixTest {

    @Test
    @DisplayName("Solves a 2x2 system with a unique solution")
    void uniqueSolution2x2() {
        SystemMatrix s = new SystemMatrix(mat(row(1, 2), row(3, 4)), vec(5, 6));
        assertVectorClose(vec(-4, 4.5), s.gaussJordan());
    }

    @Test
    @DisplayName("Solves a 3x3 system that needs a row swap")
    void uniqueSolution3x3() {
        // x = 1, y = 2, z = 3
        Matrix a = mat(row(0, 1, 1), row(2, 0, 1), row(1, 1, 0));
        Vector b = vec(5, 5, 3);
        assertVectorClose(vec(1, 2, 3), new SystemMatrix(a, b).gaussJordan());
    }

    @Test
    @DisplayName("The solution carries the elimination steps")
    void solutionHasSteps() {
        SolutionVector x = new SystemMatrix(mat(row(1, 2), row(3, 4)), vec(5, 6)).gaussJordan();
        assertFalse(x.getSteps().isEmpty());
        for (String step : x.getSteps()) {
            assertTrue(step.startsWith("R_"), "unexpected step text: " + step);
        }
    }

    @Test
    @DisplayName("An inconsistent system (0 = c) has no solution")
    void noSolution() {
        SystemMatrix s = new SystemMatrix(mat(row(1, 2), row(2, 4)), vec(5, 6));
        MultipleOrNoSolutionException e = assertThrows(MultipleOrNoSolutionException.class, s::gaussJordan);
        assertTrue(e.getMessage().contains("no solution"), e.getMessage());
    }

    @Test
    @DisplayName("Dependent rows give infinitely many solutions")
    void infinitelyManySolutions() {
        SystemMatrix s = new SystemMatrix(mat(row(1, 2), row(2, 4)), vec(5, 10));
        MultipleOrNoSolutionException e = assertThrows(MultipleOrNoSolutionException.class, s::gaussJordan);
        assertTrue(e.getMessage().contains("infinitely many"), e.getMessage());
    }

    @Test
    @DisplayName("More unknowns than equations gives infinitely many solutions")
    void moreUnknownsThanEquations() {
        SystemMatrix s = new SystemMatrix(mat(row(1, 1)), vec(2));
        assertThrows(MultipleOrNoSolutionException.class, s::gaussJordan);
    }

    @Test
    @DisplayName("b must have one value per row of A")
    void wrongRightHandSide() {
        assertThrows(IllegalArgumentException.class,
                () -> new SystemMatrix(mat(row(1, 2), row(3, 4)), vec(1, 2, 3)));
    }

    @Test
    @DisplayName("Solving never modifies the user's A and b")
    void originalsAreNotModified() {
        Matrix a = mat(row(1, 2), row(3, 4));
        Vector b = vec(5, 6);
        SystemMatrix s = new SystemMatrix(a, b);
        s.gaussJordan();
        s.gaussJordan();   // solving twice gives the same system
        assertEquals(mat(row(1, 2), row(3, 4)), a);
        assertEquals(vec(5, 6), b);
        assertEquals(vec(5, 6), s.getEqVector());
    }

    @Test
    @DisplayName("solve() (by the inverse) agrees with gaussJordan()")
    void solveAgreesWithGaussJordan() {
        SystemMatrix s = new SystemMatrix(mat(row(2, 0, 1), row(1, 3, 2), row(1, 1, 2)), vec(1, 2, 3));
        assertVectorClose(s.gaussJordan(), s.solve());
    }
}
