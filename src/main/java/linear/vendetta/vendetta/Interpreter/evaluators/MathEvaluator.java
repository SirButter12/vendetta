/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter.evaluators;

import java.util.List;
import linear.vendetta.vendetta.Interpreter.CommandException;
import static linear.vendetta.vendetta.Interpreter.Evaluator.expectArgs;

/**
 *
 * @author ian
 */
public class MathEvaluator {
    public static double sin(List<Object> args) {
        expectArgs("sin", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.sin(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double cos(List<Object> args) {
        expectArgs("cos", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.cos(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double tan(List<Object> args) {
        expectArgs("tan", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.tan(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double sec(List<Object> args) {
        expectArgs("sec", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return 1 / Math.cos(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double csc(List<Object> args) {
        expectArgs("csc", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return 1 / Math.sin(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double cot(List<Object> args) {
        expectArgs("cot", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return 1 / Math.tan(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double ln(List<Object> args) {
        expectArgs("ln", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.log(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double sqrt(List<Object> args) {
        expectArgs("sqrt", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.sqrt(x);
        }
        
        throw new CommandException("invalid argument");
    }
}
