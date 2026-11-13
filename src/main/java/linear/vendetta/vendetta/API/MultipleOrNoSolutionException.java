/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Exception.java to edit this template
 */
package linear.vendetta.vendetta.API;

/**
 *
 * @author ian
 */
public class MultipleOrNoSolutionException extends RuntimeException {

    /**
     * Creates a new instance of <code>MultipleOrNoSolutionException</code>
     * without detail message.
     */
    public MultipleOrNoSolutionException() {
    }

    /**
     * Constructs an instance of <code>MultipleOrNoSolutionException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public MultipleOrNoSolutionException(String msg) {
        super(msg);
    }
}
