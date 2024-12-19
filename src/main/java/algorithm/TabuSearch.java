package algorithm;

import model.*;
import java.util.*;

public class TabuSearch implements OptimizationAlgorithm {
    private final int tabuListSize;
    private final int maxIterations;
    private final Random random = new Random();
    
    public TabuSearch(Map<String, Object> parameters) {
        this.tabuListSize = (int) parameters.getOrDefault("tabuListSize", 20);
        this.maxIterations = (int) parameters.getOrDefault("maxIterations", 500);
    }
    
    @Override
    public Solution solve(Problem problem) {
        Solution currentSolution = generateInitialSolution(problem);
        Solution bestSolution = currentSolution.clone();
        Queue<String> tabuList = new LinkedList<>();
        
        for (int i = 0; i < maxIterations; i++) {
            List<Solution> neighbors = generateNeighbors(currentSolution, problem);
            Solution bestNeighbor = null;
            double bestNeighborCost = Double.MAX_VALUE;
            
            for (Solution neighbor : neighbors) {
                String moveKey = getMoveKey(currentSolution, neighbor);
                double cost = calculateCost(neighbor, problem);
                
                if (!tabuList.contains(moveKey) || cost < calculateCost(bestSolution, problem)) {
                    if (cost < bestNeighborCost) {
                        bestNeighborCost = cost;
                        bestNeighbor = neighbor;
                    }
                }
            }
            
            if (bestNeighbor != null) {
                currentSolution = bestNeighbor;
                if (calculateCost(bestNeighbor, problem) < calculateCost(bestSolution, problem)) {
                    bestSolution = bestNeighbor.clone();
                }
                
                String moveKey = getMoveKey(currentSolution, bestNeighbor);
                tabuList.offer(moveKey);
                if (tabuList.size() > tabuListSize) {
                    tabuList.poll();
                }
            }
        }
        
        return bestSolution;
    }
    
    @Override
    public String getName() {
        return "禁忌搜索算法(TS)";
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
    
    private List<Solution> generateNeighbors(Solution current, Problem problem) {
        List<Solution> neighbors = new ArrayList<>();
        
        // 对每个顾客尝试改变路径
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            int currentPathIndex = current.getPathIndices()[i];
            int pathCount = problem.getPaths().get("customer_" + i).size();
            
            for (int j = 0; j < pathCount; j++) {
                if (j != currentPathIndex) {
                    Solution neighbor = current.clone();
                    neighbor.setPathIndex(i, j);
                    updateSolutionMetrics(neighbor, problem);
                    neighbors.add(neighbor);
                }
            }
        }
        
        return neighbors;
    }
    
    private String getMoveKey(Solution from, Solution to) {
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < from.getPathIndices().length; i++) {
            if (from.getPathIndices()[i] != to.getPathIndices()[i]) {
                key.append(i).append(":").append(from.getPathIndices()[i])
                   .append("->").append(to.getPathIndices()[i]).append(";");
            }
        }
        return key.toString();
    }
    
    private double calculateCost(Solution solution, Problem problem) {
        double totalCost = 0;
        double totalTime = 0;
        
        for (int i = 0; i < solution.getPathIndices().length; i++) {
            Path path = problem.getPaths().get("customer_" + i).get(solution.getPathIndices()[i]);
            totalCost += path.getDistance() + path.getCost();
            totalTime += path.getTime();
        }
        
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
} 