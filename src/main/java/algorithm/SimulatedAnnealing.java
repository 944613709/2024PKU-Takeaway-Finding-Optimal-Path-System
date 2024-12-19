package algorithm;

import model.*;
import java.util.*;

public class SimulatedAnnealing implements OptimizationAlgorithm {
    private final double initialTemp;
    private final double coolingRate;
    private final double finalTemp;
    private final Random random = new Random();
    
    public SimulatedAnnealing(Map<String, Object> parameters) {
        this.initialTemp = (double) parameters.getOrDefault("initialTemp", 100.0);
        this.coolingRate = (double) parameters.getOrDefault("coolingRate", 0.95);
        this.finalTemp = (double) parameters.getOrDefault("finalTemp", 0.01);
    }
    
    @Override
    public Solution solve(Problem problem) {
        Solution currentSolution = generateInitialSolution(problem);
        Solution bestSolution = currentSolution.clone();
        double temperature = initialTemp;
        
        while (temperature > finalTemp) {
            Solution newSolution = generateNeighbor(currentSolution, problem);
            double currentEnergy = calculateEnergy(currentSolution, problem);
            double newEnergy = calculateEnergy(newSolution, problem);
            
            if (acceptanceProbability(currentEnergy, newEnergy, temperature) > random.nextDouble()) {
                currentSolution = newSolution;
                if (newEnergy < calculateEnergy(bestSolution, problem)) {
                    bestSolution = newSolution.clone();
                }
            }
            
            temperature *= coolingRate;
        }
        
        return bestSolution;
    }
    
    @Override
    public String getName() {
        return "模拟退火算法(SA)";
    }
    
    private double acceptanceProbability(double currentEnergy, double newEnergy, double temperature) {
        if (newEnergy < currentEnergy) return 1.0;
        return Math.exp((currentEnergy - newEnergy) / temperature);
    }
    
    private Solution generateInitialSolution(Problem problem) {
        Solution solution = new Solution(problem.getCustomers().size());
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            int pathIndex = random.nextInt(problem.getPaths().get("customer_" + i).size());
            solution.setPathIndex(i, pathIndex);
        }
        updateSolutionMetrics(solution, problem);
        return solution;
    }
    
    private Solution generateNeighbor(Solution current, Problem problem) {
        Solution neighbor = current.clone();
        // 随机选择一个顾客并改变其路径
        int customerIndex = random.nextInt(problem.getCustomers().size());
        int newPathIndex = random.nextInt(problem.getPaths().get("customer_" + customerIndex).size());
        neighbor.setPathIndex(customerIndex, newPathIndex);
        updateSolutionMetrics(neighbor, problem);
        return neighbor;
    }
    
    private double calculateEnergy(Solution solution, Problem problem) {
        double totalCost = 0;
        double totalTime = 0;
        
        for (int i = 0; i < solution.getPathIndices().length; i++) {
            Path path = problem.getPaths().get("customer_" + i).get(solution.getPathIndices()[i]);
            totalCost += path.getDistance() + path.getCost();
            totalTime += path.getTime();
        }
        
        // 添加时间约束惩罚
        if (totalTime > problem.getTimeConstraint()) {
            totalCost += (totalTime - problem.getTimeConstraint()) * 1000;
        }
        
        return totalCost;
    }
    
    private void updateSolutionMetrics(Solution solution, Problem problem) {
        double totalCost = 0;
        double totalTime = 0;
        
        for (int i = 0; i < solution.getPathIndices().length; i++) {
            Path path = problem.getPaths().get("customer_" + i).get(solution.getPathIndices()[i]);
            totalCost += path.getDistance() + path.getCost();
            totalTime += path.getTime();
        }
        
        solution.setTotalCost(totalCost);
        solution.setTotalTime(totalTime);
    }
    
    // 其他辅助方法...
} 