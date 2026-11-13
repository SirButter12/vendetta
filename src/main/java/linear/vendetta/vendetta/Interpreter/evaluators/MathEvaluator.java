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
    
    public static double arcsin(List<Object> args) {
        expectArgs("arcsin", args, 1);
        
        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) > 1) {
                throw new CommandException("arcsin is only defined for -1 <= x <= 1");
            }
            return Math.asin(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double arccos(List<Object> args) {
        expectArgs("arccos", args, 1);
        
        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) > 1) {
                throw new CommandException("arccos is only defined for -1 <= x <= 1");
            }
            return Math.acos(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double arctan(List<Object> args) {
        expectArgs("arctan", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.atan(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double arcsec(List<Object> args) {
        expectArgs("arcsec", args, 1);
        
        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) < 1) {
                throw new CommandException("arcsec is only defined for |x| >= 1");
            }
            return Math.acos(1 / x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double arccsc(List<Object> args) {
        expectArgs("arccsc", args, 1);
        
        if (args.get(0) instanceof Double x) {
            if (Math.abs(x) < 1) {
                throw new CommandException("arccsc is only defined for |x| >= 1");
            }
            return Math.asin(1 / x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double arccot(List<Object> args) {
        expectArgs("arccot", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.PI / 2 - Math.atan(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double log(List<Object> args) {
        expectArgs("log", args, 2);
        
        if (args.get(0) instanceof Double x && args.get(1) instanceof Double base) {
            if (base <= 0 || base == 1) {
                throw new CommandException("log base must be positive and different from 1");
            }
            return Math.log(x) / Math.log(base);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double log10(List<Object> args) {
        expectArgs("log10", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.log10(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static double mod(List<Object> args) {
        expectArgs("mod", args, 2);
        
        if (args.get(0) instanceof Double a && args.get(1) instanceof Double b) {
            if (b == 0) {
                throw new CommandException("mod by zero is not defined");
            }
            return ((a % b) + b) % b;
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
