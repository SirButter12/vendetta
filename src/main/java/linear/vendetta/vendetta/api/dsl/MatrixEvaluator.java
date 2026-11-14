package linear.vendetta.vendetta.api.dsl;

import java.util.List;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import static linear.vendetta.vendetta.api.dsl.Evaluator.expectArgs;
import static linear.vendetta.vendetta.api.dsl.Evaluator.toSize;
/**
 * UNUSED: old copy of evaluators.MatrixEvaluator (all methods are private).
 * Evaluator imports the one in the evaluators package. Safe to delete.
 *
 * @author ian
 */
public class MatrixEvaluator {
     private static Matrix adj(List<Object> args) {
        expectArgs("adj", args, 1);

        if (args.get(0) instanceof Matrix m) {
            return Matrix.adj(m);
        }

        throw new CommandException("Invalid argument for function");
    }

    private static Matrix inv(List<Object> args) {
        expectArgs("inv", args, 1);

        if (args.get(0) instanceof Matrix m) {
            return Matrix.inverse(m);
        }

        throw new CommandException("Invalid argument for function");
    }

    private static Matrix trans(List<Object> args) {
        expectArgs("trans", args, 1);
        if (args.get(0) instanceof Matrix m) {

            return m.transpose();
        }

        throw new CommandException("Invalid argument for function");
    }

    private static double det(List<Object> args) {
        expectArgs("det", args, 1);

        if (args.get(0) instanceof Matrix m) {

            return m.getDeterminant();
        }

        throw new CommandException("Invalid argument for function");
    }

    private static Object triangulize(List<Object> args) {
        expectArgs("triangulize", args, 1);

        if (args.get(0) instanceof Matrix m) {
            return m.toTriangle();
        }

        throw new CommandException("invalid argument");
    }

    private static Object matrix(List<Object> args) {
        expectArgs("matrix", args, 2);
        return new Matrix(toSize(args.get(0)), toSize(args.get(1)));
    }


}
