/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import linear.vendetta.vendetta.API.Matrix;
import linear.vendetta.vendetta.API.Vector;

/**
 *
 * @author ian
 */
public class Interpreter {
    private static HashMap<String, Object> memory = new HashMap();
    private static final Set<String> CONSTANTS = Set.of("pi", "e");
    
    static {
        // Vectores
        memory.put("A", new Vector(List.of(1, 2, 3)));
        memory.put("B", new Vector(List.of(2, -1, 3)));
        memory.put("C", new Vector(List.of(-1, 2)));
        memory.put("D", new Vector(List.of(-1, 2)));

        // Matrices
        memory.put("M1", Matrix.build(List.of(
                new Vector(List.of(1, 2, 6)),
                new Vector(List.of(3, -2, 3)),
                new Vector(List.of(0, -1, 5))
        )));
        
        memory.put("M2", Matrix.build(List.of(
                new Vector(List.of(6, 7, 6)),
                new Vector(List.of(4, -8, 6)),
                new Vector(List.of(0, -1, 9))
        )));
        
        
        memory.put("I", Matrix.build(List.of(
                new Vector(List.of(1, 0, 0)),
                new Vector(List.of(0, 1, 0)),
                new Vector(List.of(0, 0, 1))
        )));
        
        memory.put("i", Matrix.build(List.of(
                new Vector(List.of(0, -1)),
                new Vector(List.of(1, 0))
        )));
        
        memory.put("M3", Matrix.build(List.of(
                new Vector(List.of(1, 2, 3, 4)),
                new Vector(List.of(1, 2, 3, 4)),
                new Vector(List.of(1, 2, 3, 4))
        )));   
          
    
        
        memory.put("pi", Math.PI);
        memory.put("e", Math.E);
    }
    
    public static List<CompiledResult> compile(List<String> commands) {
        List<CompiledResult> results = new ArrayList<CompiledResult>();
        for (String command: commands) {
            Tokenizer tokenizedCommand = new Tokenizer(command);
            
            Parser parser = new Parser(tokenizedCommand.getTokens());
            
            Node commandTree = parser.parse();
            
            results.add(new CompiledResult(command, Evaluator.evaluateNode(commandTree)));
        }    
        
        return results;
    }
    
    public static void assignToMemory(String name, Object obj) {
        if (CONSTANTS.contains(name)) {
            throw new CommandException(name + " is a constant and cannot be reassigned");
        }
        
        memory.put(name, obj);
    }
    
    public static HashMap<String, Object> getMemory() {
        return memory;
    }    
}
