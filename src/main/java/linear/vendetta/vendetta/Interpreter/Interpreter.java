/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ian
 */
public class Interpreter {
    private static HashMap<String, Object> memory = new HashMap();
    private static final Set<String> CONSTANTS = Set.of("pi", "e");
    
    static {
        if (Interpreter.class.getResource("/coconut.jpg") == null) throw new Error("coconut.jpg not found. boom boom no linear algebra for you");
    
        
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
