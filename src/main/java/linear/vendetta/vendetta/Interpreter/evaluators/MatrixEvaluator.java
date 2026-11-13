/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.Interpreter.evaluators;

import java.util.List;
import linear.vendetta.vendetta.API.Matrix;
import linear.vendetta.vendetta.Interpreter.CommandException;
import static linear.vendetta.vendetta.Interpreter.Evaluator.expectArgs;
import static linear.vendetta.vendetta.Interpreter.Evaluator.toSize;
/**
 *
 * @author ian
 */
public class MatrixEvaluator {
     public static Matrix adj(List<Object> args) {
        expectArgs("adj", args, 1);
        
        if (args.get(0) instanceof Matrix m) {
            return Matrix.adj(m);
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    public static Matrix inv(List<Object> args) {
        expectArgs("inv", args, 1);
        
        if (args.get(0) instanceof Matrix m) {    
            return Matrix.inverse(m);
        }
        
        throw new CommandException("Invalid argument for function");
    }

    public static Matrix trans(List<Object> args) {
        expectArgs("trans", args, 1);
        if (args.get(0) instanceof Matrix m) {
            
            return m.transpose();
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    public static double det(List<Object> args) {
        expectArgs("det", args, 1);
        
        if (args.get(0) instanceof Matrix m) {
            
            return m.getDeterminant();
        }
        
        throw new CommandException("Invalid argument for function");
    }
    
    public static Object triangulize(List<Object> args) {
        expectArgs("triangulize", args, 1);
        
        if (args.get(0) instanceof Matrix m) {
            return m.toTriangle();
        }
        
        throw new CommandException("invalid argument");
    }
    
    public static Object matrix(List<Object> args) {
        expectArgs("matrix", args, 2);
        return new Matrix(toSize(args.get(0)), toSize(args.get(1)));
    }
    
    
}
