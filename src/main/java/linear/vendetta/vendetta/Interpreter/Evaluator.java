/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                
            default -> throw new IllegalStateException("Unexpected value: " + (node.text()));
        };
    }    
    
    private static Object adj(List<Object> args) {
        if (args.get(0) instanceof Matrix m) {
            if (args.size() != 1) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
            return Matrix.adj(m);
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object inv(List<Object> args) {
        if (args.get(0) instanceof Matrix m) {
            if (args.size() != 1) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
            return Matrix.inverse(m);
        }
        
        throw new CommandException("Invalid argument for function");
    }

    private static Object trans(List<Object> args) {
        if (args.get(0) instanceof Matrix m) {
            if (args.size() != 1) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
            return m.transpose();
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object det(List<Object> args) {
        if (args.get(0) instanceof Matrix m) {
            if (args.size() != 1) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
            return m.getDeterminant();
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object dot(List<Object> args) {
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            if (args.size() != 2) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
            return v1.dotProduct(v2);
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    private static Object cross(List<Object> args) {
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            if (args.size() != 2) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
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
        if (args.get(0) instanceof Vector v1 && args.get(1) instanceof Vector v2) {
            if (args.size() != 2) {
                throw new CommandException("Invalid argument, amount not compatible");
            }
            
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
            return m1.multiply(m2);
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
            return m1.multiply(Matrix.inverse(m2));
        }
     
        throw new CommandException("Cannot divide " + o1.getClass().getSimpleName() + " by " + o2.getClass().getSimpleName());
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