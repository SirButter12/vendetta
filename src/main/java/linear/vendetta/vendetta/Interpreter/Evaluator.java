package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;
import linear.vendetta.vendetta.Interpreter.Node.NodeKind;
import static linear.vendetta.vendetta.Interpreter.evaluators.MathEvaluator.*;
import static linear.vendetta.vendetta.Interpreter.evaluators.MatrixEvaluator.*;
import static linear.vendetta.vendetta.Interpreter.evaluators.ArithmeticEvaluator.*;
import static linear.vendetta.vendetta.Interpreter.evaluators.VectorEvaluator.*;

/**
 *
 * @author ian
 */
public class Evaluator {
    private static HashMap<String, Object> memory = Interpreter.getMemory();
    
    public static Object evaluateNode(Node root) {
        
        return switch (root.kind()) {
            case NodeKind.ASSIGN -> assign(root);
                
            case NodeKind.BINARY -> bin(root);
                
            case NodeKind.CALL -> call(root);
                
            case NodeKind.NAME -> lookup(root.text());  
                
            case NodeKind.NUMBER -> Double.parseDouble(root.text());                
        };
       
    }
    
    private static Object lookup(String name) {
        Object value = memory.get(name);
        if (value == null) {
            throw new CommandException(name + " is used but never initialized!");
        }
        return value;
    }
    
    private static Object assign(Node root) {
        Object value = evaluateNode(root.children().get(0));
        
        Interpreter.assignToMemory(root.text(), value);
        
        return value;
    }

    private static Object bin(Node node) {
        Object left  = evaluateNode(node.children().get(0));
        Object right = evaluateNode(node.children().get(1));
        
        return switch(node.text()) {
            case "+" -> sum(left, right);
            case "-" -> sub(left, right);
            case "*" -> times(left, right);
            case "/" -> div(left, right);    
            case "^" -> exp(left, right);   
            default -> throw new IllegalStateException("Unexpected value: " + (node.text()));
        };
    }
    
    //"Adj","Inv","Trans","Det","Dot", "Cross", "Project", "SolveSystem", "Vector", "Matrix"
    private static Object call(Node node) {
        List<Object> args = new ArrayList<>();
        
        for (Node child : node.children()) {
            args.add(evaluateNode(child));
        }
        
        return switch(node.text()) {
            case "adj" -> adj(args);
            case "inv" -> inv(args);
            case "trans" -> trans(args);
            case "det" -> det(args);
            case "dot" -> dot(args);
            case "cross" -> cross(args);    
            case "project" -> project(args);
            case "solveSystem" -> solveSystem(args);
            case "triangulize" -> triangulize(args);
                
            //MATH    
            case "sin" -> sin(args);
            case "cos" -> cos(args);
            case "tan" -> tan(args);   
            case "sec" -> sec(args);
            case "csc" -> csc(args);
            case "cot" -> cot(args);
            case "arcsin" -> arcsin(args);
            case "arccos" -> arccos(args);
            case "arctan" -> arctan(args);
            case "arcsec" -> arcsec(args);
            case "arccsc" -> arccsc(args);
            case "arccot" -> arccot(args);
            case "ln" -> ln(args);
            case "sqrt" -> sqrt(args);
            case "log" -> log(args);
            case "log10" -> log10(args);
            case "mod" -> mod(args);
                
                
            default -> throw new IllegalStateException("Unexpected value: " + (node.text()));
        };
    }    
    
    private static Object solveSystem(List<Object> args) {
        //to be done
        return null;
    }
        
    public static void expectArgs(String fn, List<Object> args, int n) {
        if (args.size() != n) {
            throw new CommandException(fn + " needs " + n + " argument(s)");
        }
    }    
    
    public static int toSize(Object o) {
        if (o instanceof Double d && d == Math.floor(d) && d > 0) {
            return d.intValue();
        }
        throw new CommandException("Sizes must be whole positive numbers");
    }
}