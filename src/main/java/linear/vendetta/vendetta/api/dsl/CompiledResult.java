package linear.vendetta.vendetta.api.dsl;

/**
 * One command and its result (Double, Vector or Matrix).
 *
 * @author ian
 */
public class CompiledResult {
    private String command;
    private Object result;

    /** @param result Double, Vector or Matrix */
    public CompiledResult(String command, Object result) {
        this.command = command;
        this.result = result;
    }

    /** Original command text. */
    public String getCommand() {
        return command;
    }

    /** Double, Vector or Matrix. */
    public Object getResult() {
        return result;
    }
}
