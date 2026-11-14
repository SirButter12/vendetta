/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package linear.vendetta.vendetta.api.linear_algebra;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ian
 */
public class SolutionVector extends Vector {
    public List<String> steps = new ArrayList<>();
    
    public SolutionVector(Vector v, List<String> steps) {
        super(v);
        this.steps = steps;
    }
    
    public List<String> getSteps() {
        return List.copyOf(steps);
    }
}
