package linear.vendetta.vendetta.api.dsl.evaluators;

import java.util.List;
import linear.vendetta.vendetta.api.linear_algebra.Matrix;
import linear.vendetta.vendetta.api.dsl.CommandException;
import static linear.vendetta.vendetta.api.dsl.Evaluator.expectArgs;
import static linear.vendetta.vendetta.api.dsl.Evaluator.toSize;
/**
 * Matrix functions callable from commands.
 *
 * @author ian
 */
public class MatrixEvaluator {
     /** adj(M): adjugate. */
     public static Matrix adj(List<Object> args) {
        expectArgs("adj", args, 1);

        if (args.get(0) instanceof Matrix m) {
            return Matrix.adj(m);
        }

        throw new CommandException("Invalid argument for function");
    }

    /** inv(M): inverse. */
    public static Matrix inv(List<Object> args) {
        expectArgs("inv", args, 1);

        if (args.get(0) instanceof Matrix m) {
            return Matrix.inverse(m);
        }

        throw new CommandException("Invalid argument for function");
    }

    /** trans(M): transpose. */
    public static Matrix trans(List<Object> args) {
        expectArgs("trans", args, 1);
        if (args.get(0) instanceof Matrix m) {

            return m.transpose();
        }

        throw new CommandException("Invalid argument for function");
    }

    /** det(M): determinant (square only). */
    public static double det(List<Object> args) {
        expectArgs("det", args, 1);

        if (args.get(0) instanceof Matrix m) {

            return m.getDeterminant();
        }

        throw new CommandException("Invalid argument for function");
    }

    /** triangulize(M): row echelon form (Gaussian elimination). */
    public static Object triangulize(List<Object> args) {
        expectArgs("triangulize", args, 1);

        if (args.get(0) instanceof Matrix m) {
            return m.toTriangle();
        }

        throw new CommandException("invalid argument");
    }

    /** Matrix(n, m) declaration -> zero n x m matrix. */
    public static Object matrix(List<Object> args) {
        expectArgs("matrix", args, 2);
        return new Matrix(toSize(args.get(0)), toSize(args.get(1)));
    }


}
