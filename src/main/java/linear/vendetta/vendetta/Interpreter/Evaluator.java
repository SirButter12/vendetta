package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;
import linear.vendetta.vendetta.API.Matrix;
import linear.vendetta.vendetta.API.Vector;
import linear.vendetta.vendetta.Interpreter.Node.NodeKind;

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
            case "Vector" -> vector(args);
            case "Matrix" -> matrix(args);
            case "triangulize" -> triangulize(args);
            case "sin" -> sin(args);
            case "cos" -> cos(args);
            case "tan" -> tan(args);   
            case "sec" -> sec(args);
            case "csc" -> csc(args);
            case "cot" -> cot(args);
            case "ln" -> ln(args);
            case "sqrt" -> sqrt(args);
                
                
            default -> throw new IllegalStateException("Unexpected value: " + (node.text()));
        };
    }    
    
    private static Object adj(List<Object> args) {
        expectArgs("adj", args, 1);
        
        if (args.get(0) instanceof Matrix m) {
            return Matrix.adj(m);
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object inv(List<Object> args) {
        expectArgs("inv", args, 1);
        
        if (args.get(0) instanceof Matrix m) {    
            return Matrix.inverse(m);
        }
        
        throw new CommandException("Invalid argument for function");
    }

    private static Object trans(List<Object> args) {
        expectArgs("trans", args, 1);
        if (args.get(0) instanceof Matrix m) {
            
            return m.transpose();
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object det(List<Object> args) {
        expectArgs("det", args, 1);
        
        if (args.get(0) instanceof Matrix m) {
            
            return m.getDeterminant();
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object dot(List<Object> args) {
        expectArgs("dot", args, 2);
        
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            return v1.dotProduct(v2);
        }
        
        if (args.get(0) instanceof Matrix m1 && args.get(1) instanceof Matrix m2) {
            return Matrix.multiply(m1, m2);
        }
        
        if (args.get(0) instanceof Matrix m && args.get(1) instanceof Vector v) {
            return Matrix.multiply(m, v);
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object cross(List<Object> args) {
        expectArgs("cross", args, 2);
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            
            if (v1.getDimension() == 2 && v2.getDimension() == 2) {
                return v1.cross2D(v2);
            }
            
            else if (v1.getDimension() == 3 && v2.getDimension() == 3) {
                return v1.cross3D(v2);
            }
            
            throw new CommandException("Invalid vector dimension");
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object project(List<Object> args) {
        expectArgs("project", args, 2);
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            
            
            return v1.projectOn(v2);
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object solveSystem(List<Object> args) {
        //to be done
        return new Vector(List.of(1, 2, 3));
    }
    
    private static Object vector(List<Object> args) {
        expectArgs("vector", args, 1);
        return new Vector(toSize(args.get(0)));
    }

    private static Object matrix(List<Object> args) {
        expectArgs("matrix", args, 2);
        return new Matrix(toSize(args.get(0)), toSize(args.get(1)));
    }

    private static int toSize(Object o) {
        if (o instanceof Double d && d == Math.floor(d) && d > 0) {
            return d.intValue();
        }
        throw new CommandException("Sizes must be whole positive numbers");
    }
    
    private static Object triangulize(List<Object> args) {
        expectArgs("triangulize", args, 1);
        
        if (args.get(0) instanceof Matrix m) {
            return m.toTriangle();
        }
        
        throw new CommandException("invalid argument");
    }
        
    
    private static Object sin(List<Object> args) {
        expectArgs("sin", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.sin(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object cos(List<Object> args) {
        expectArgs("cos", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.cos(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object tan(List<Object> args) {
        expectArgs("tan", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.tan(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object sec(List<Object> args) {
        expectArgs("sec", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return 1 / Math.cos(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object csc(List<Object> args) {
        expectArgs("csc", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return 1 / Math.sin(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object cot(List<Object> args) {
        expectArgs("cot", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return 1 / Math.tan(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object ln(List<Object> args) {
        expectArgs("ln", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.log(x);
        }
        
        throw new CommandException("invalid argument");
    }
    
    private static Object sqrt(List<Object> args) {
        expectArgs("sqrt", args, 1);
        
        if (args.get(0) instanceof Double x) {
            return Math.sqrt(x);
        }
        
        throw new CommandException("invalid argument");
    }     
    
    private static void expectArgs(String fn, List<Object> args, int n) {
        if (args.size() != n) {
            throw new CommandException(fn + " needs " + n + " argument(s)");
        }
    }    
    
    private static Object sum(Object o1, Object o2) {
        if (o1 instanceof Vector v1 && o2 instanceof Vector v2) {
            return v1.add(v2);
        }
        
        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return m1.add(m2);
        }
        
        if (o1 instanceof Double x && o2 instanceof Double y) {
            return x + y;
        }
        
        throw new CommandException("Cannot add " + o1.getClass().getSimpleName() + " to " + o2.getClass().getSimpleName());
    }
    
    private static Object sub(Object o1, Object o2) {
        if (o1 instanceof Vector v1 && o2 instanceof Vector v2) {
            return v1.subtract(v2);
        }
        
        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return m1.subtract(m2);
        }
        
        if (o1 instanceof Double x && o2 instanceof Double y) {
            return x - y;
        }
        
        throw new CommandException("Cannot subtract " + o2.getClass().getSimpleName() + " from " + o1.getClass().getSimpleName());
    }
    
    private static Object times(Object o1, Object o2) {
        if (o1 instanceof Double x && o2 instanceof Double y) {
            return x * y;
        }
        
        if (o1 instanceof Double x) {
            return scale(o2, x);                
        }
        
        if (o2 instanceof Double y) {
            return scale(o1, y);                
        }
        
        if (o1 instanceof Vector v1 && o2 instanceof Vector v2) {
            return v1.dotProduct(v2);
        }
        
        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return Matrix.multiply(m1, m2);
        }
        
        if (o1 instanceof Matrix m && o2 instanceof Vector v) {
            return Matrix.multiply(m, v);
        }
             
        throw new CommandException("Cannot multiply " + o1.getClass().getSimpleName() + " by " + o2.getClass().getSimpleName());
    }
    
    private static Object div(Object o1, Object o2) {
        if (o1 instanceof Double x && o2 instanceof Double y) {
        return x / y;
        }
        
        if (o2 instanceof Double x) {
            return scale(o1, 1 / x);                
        }
        
        if (o1 instanceof Matrix m1 && o2 instanceof Matrix m2) {
            return Matrix.multiply(m1, Matrix.inverse(m2));
        }
     
        throw new CommandException("Cannot divide " + o1.getClass().getSimpleName() + " by " + o2.getClass().getSimpleName());
    }
    
    private static Object exp(Object o1, Object o2) {
        if (o1 instanceof Double x && o2 instanceof Double y) {
            return Math.pow(x, y);
        }
        
        throw new CommandException("^ only works for numbers!!!");
    }
    
    
    private static Object scale(Object o, double k) {
        if (o instanceof Matrix m) {
            return m.scalarMult(k);
        }
        
        if (o instanceof Vector v) {
            return v.scalarMult(k);
        }
        
        throw new CommandException("Expected a matrix or a vector");
    }
}