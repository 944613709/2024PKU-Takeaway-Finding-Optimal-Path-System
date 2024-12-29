package algorithm;

import model.*;
import java.util.*;

/**
 * 回溯算法实现类
 * 通过深度优先搜索遍历所有可能的路径组合
 * 当找不到满足时间约束的解时，使用贪心策略作为备选方案
 */
public class BacktrackingAlgorithm implements OptimizationAlgorithm {
    private double bestTotalCost;         // 当前找到的最优总成本
    private int[] bestPath;               // 最优路径选择
    private double bestTotalTime;         // 最优解对应的总时间
    private boolean foundValidSolution;    // 是否找到有效解的标志
    
    public BacktrackingAlgorithm(Map<String, Object> parameters) {
    }
    
    @Override
    public Solution solve(Problem problem) {
        int n = problem.getCustomers().size();
        bestTotalCost = Double.MAX_VALUE;
        bestPath = new int[n];
        bestTotalTime = 0;
        foundValidSolution = false;
        
        // 初始化当前路径
        int[] currentPath = new int[n];
        Arrays.fill(currentPath, -1);
        
        // 开始回溯搜索
        backtrack(problem, 0, 0.0, 0.0, currentPath);
        
        // 如果回溯没找到可行解，使用贪心策略
        if (!foundValidSolution) {
            return generateGreedySolution(problem);
        }
        
        // 构建解决方案
        Solution solution = new Solution(n);
        for (int i = 0; i < n; i++) {
            solution.setPathIndex(i, bestPath[i]);
        }
        solution.setTotalCost(bestTotalCost);
        solution.setTotalTime(bestTotalTime);
        
        return solution;
    }
    
    /**
     * 回溯搜索核心函数
     * @param problem 问题实例
     * @param customerIndex 当前处理的顾客索引
     * @param currentTime 当前累计时间
     * @param currentCost 当前累计成本
     * @param currentPath 当前路径选择
     */
    private void backtrack(Problem problem, int customerIndex, double currentTime, 
                         double currentCost, int[] currentPath) {
        // 剪枝：如果当前时间已超过限制，或当前成本已超过最优解，则停止搜索
        if (currentTime > problem.getTimeConstraint() || currentCost >= bestTotalCost) {
            return;
        }
        
        // 找到一个完整的解
        if (customerIndex == problem.getCustomers().size()) {
            foundValidSolution = true;  // 标记找到有效解
            // 更新最优解
            if (currentCost < bestTotalCost) {
                bestTotalCost = currentCost;
                bestTotalTime = currentTime;
                System.arraycopy(currentPath, 0, bestPath, 0, currentPath.length);
            }
            return;
        }
        
        // 获取当前顾客的所有可能路径
        List<Path> paths = problem.getPaths().get("customer_" + customerIndex);
        
        // 尝试每条可能的路径
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            double newTime = currentTime + path.getTime();
            double newCost = currentCost + path.getDistance() + path.getCost();
            
            // 如果满足时间约束且有可能得到更优解，继续搜索
            if (newTime <= problem.getTimeConstraint() && newCost < bestTotalCost) {
                currentPath[customerIndex] = i;
                backtrack(problem, customerIndex + 1, newTime, newCost, currentPath);
            }
        }
    }
    
    /**
     * 改进的贪心策略解决方案
     * 优先考虑满足时间约束，当无法满足时选择耗时最短的路径
     */
    private Solution generateGreedySolution(Problem problem) {
        int n = problem.getCustomers().size();
        Solution solution = new Solution(n);
        double totalTime = 0;
        double totalCost = 0;
        double timeConstraint = problem.getTimeConstraint();
        
        for (int i = 0; i < n; i++) {
            List<Path> paths = problem.getPaths().get("customer_" + i);
            int bestPathIndex = 0;
            double bestScore = Double.MAX_VALUE;
            
            // 使用综合评分来选择路径
            for (int j = 0; j < paths.size(); j++) {
                Path path = paths.get(j);
                // 计算综合评分：成本 + 时间超出惩罚
                double timePenalty = Math.max(0, totalTime + path.getTime() - timeConstraint);
                double score = (path.getDistance() + path.getCost()) + timePenalty * 1000; // 惩罚因子为1000
                
                if (score < bestScore) {
                    bestScore = score;
                    bestPathIndex = j;
                }
            }
            
            // 更新解决方案
            solution.setPathIndex(i, bestPathIndex);
            Path selectedPath = paths.get(bestPathIndex);
            totalTime += selectedPath.getTime();
            totalCost += selectedPath.getDistance() + selectedPath.getCost();
        }
        
        // 如果总时间超过约束，添加惩罚
        if (totalTime > timeConstraint) {
            totalCost += (totalTime - timeConstraint) * 1000;
        }
        
        solution.setTotalCost(totalCost);
        solution.setTotalTime(totalTime);
        return solution;
    }
    
    @Override
    public String getName() {
        return "回溯算法(Backtracking)";
    }
} 