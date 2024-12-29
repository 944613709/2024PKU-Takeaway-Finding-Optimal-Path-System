package algorithm;

import model.*;
import java.util.*;
@Deprecated
public class DynamicProgramming implements OptimizationAlgorithm {
    private final int maxStates;
    private Map<String, CacheEntry> memo;
    
    private static class CacheEntry {
        double cost;
        int[] path;
        
        CacheEntry(double cost, int[] path) {
            this.cost = cost;
            this.path = path.clone();
        }
    }
    
    public DynamicProgramming(Map<String, Object> parameters) {
        this.maxStates = (int) parameters.getOrDefault("maxStates", 1000000);
        this.memo = new HashMap<>();
    }
    
    @Override
    public Solution solve(Problem problem) {
        memo.clear();
        int[] bestPath = new int[problem.getCustomers().size()];
        CacheEntry result = findMinCost(problem, 0, 0.0, new int[problem.getCustomers().size()]);
        
        if (result == null || result.cost == Double.MAX_VALUE) {
            // 如果没找到可行解，使用贪心策略
            return generateGreedySolution(problem);
        }
        
        Solution solution = new Solution(problem.getCustomers().size());
        for (int i = 0; i < result.path.length; i++) {
            solution.setPathIndex(i, result.path[i]);
        }
        updateSolutionMetrics(solution, problem);
        
        return solution;
    }
    
    @Override
    public String getName() {
        return "动态规划算法(DP)";
    }
    
    private CacheEntry findMinCost(Problem problem, int customerIndex, double currentTime, int[] currentPath) {
        if (currentTime > problem.getTimeConstraint()) {
            return new CacheEntry(Double.MAX_VALUE, currentPath);
        }
        
        if (customerIndex == problem.getCustomers().size()) {
            return new CacheEntry(0.0, currentPath);
        }
        
        String state = getState(customerIndex, currentTime);
        if (memo.containsKey(state)) {
            return memo.get(state);
        }
        
        double minCost = Double.MAX_VALUE;
        int[] bestPath = currentPath.clone();
        List<Path> paths = problem.getPaths().get("customer_" + customerIndex);
        
        // 首先尝试时间最短的路径
        int[] sortedIndices = getSortedPathIndices(paths);
        
        for (int i = 0; i < paths.size(); i++) {
            int pathIndex = sortedIndices[i];
            Path path = paths.get(pathIndex);
            double newTime = currentTime + path.getTime();
            
            if (newTime <= problem.getTimeConstraint()) {
                currentPath[customerIndex] = pathIndex;
                CacheEntry nextResult = findMinCost(problem, customerIndex + 1, newTime, currentPath.clone());
                
                if (nextResult.cost != Double.MAX_VALUE) {
                    double totalCost = path.getDistance() + path.getCost() + nextResult.cost;
                    if (totalCost < minCost) {
                        minCost = totalCost;
                        bestPath = nextResult.path.clone();
                        bestPath[customerIndex] = pathIndex;
                    }
                }
            }
        }
        
        CacheEntry result = new CacheEntry(minCost, bestPath);
        memo.put(state, result);
        return result;
    }
    
    private int[] getSortedPathIndices(List<Path> paths) {
        Integer[] indices = new Integer[paths.size()];
        for (int i = 0; i < paths.size(); i++) {
            indices[i] = i;
        }
        
        Arrays.sort(indices, (a, b) -> Double.compare(paths.get(a).getTime(), paths.get(b).getTime()));
        
        int[] result = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            result[i] = indices[i];
        }
        return result;
    }
    
    private Solution generateGreedySolution(Problem problem) {
        Solution solution = new Solution(problem.getCustomers().size());
        double totalTime = 0;
        
        // 对每个顾客选择满足时间约束且成本最低的路径
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            List<Path> paths = problem.getPaths().get("customer_" + i);
            int bestPathIndex = 0;
            double minCost = Double.MAX_VALUE;
            double bestTime = Double.MAX_VALUE;
            
            for (int j = 0; j < paths.size(); j++) {
                Path path = paths.get(j);
                if (totalTime + path.getTime() <= problem.getTimeConstraint()) {
                    double cost = path.getDistance() + path.getCost();
                    if (cost < minCost || (cost == minCost && path.getTime() < bestTime)) {
                        minCost = cost;
                        bestTime = path.getTime();
                        bestPathIndex = j;
                    }
                }
            }
            
            solution.setPathIndex(i, bestPathIndex);
            totalTime += paths.get(bestPathIndex).getTime();
        }
        
        updateSolutionMetrics(solution, problem);
        return solution;
    }
    
    private String getState(int customerIndex, double currentTime) {
        return customerIndex + ":" + Math.round(currentTime * 10) / 10.0;  // 减少状态空间
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