package linear.vendetta.vendetta.API;

/**
 * Thrown when a linear system has no solution or infinitely many.
 *
 * @author ian
 */
public class MultipleOrNoSolutionException extends RuntimeException {

    /** No message. */
    public MultipleOrNoSolutionException() {
    }

    /** @param msg text shown to the user */
    public MultipleOrNoSolutionException(String msg) {
        super(msg);
    }
}
