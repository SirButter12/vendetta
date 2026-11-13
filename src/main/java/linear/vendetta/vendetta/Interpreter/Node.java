/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.List;

/**
 *
 * @author ian
 */

public record Node(NodeKind kind, String text, List<Node> children) {

    public enum NodeKind { 
        NUMBER, NAME, CALL, BINARY, ASSIGN, DECLARE
    }

    public Node {
        children = List.copyOf(children); 
    }
}

