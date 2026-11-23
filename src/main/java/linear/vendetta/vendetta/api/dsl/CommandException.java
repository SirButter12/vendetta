package linear.vendetta.vendetta.api.dsl;

/**
 * Error in a user command (bad syntax, wrong types, unknown variable...).
 * The message is meant to be shown to the user.
 *
 * @author ian
 */
public class CommandException extends RuntimeException {

    /** @param msg text shown to the user. Always give one: the UI shows it as is. */
    public CommandException(String msg) {
        super(msg);
    }
}
