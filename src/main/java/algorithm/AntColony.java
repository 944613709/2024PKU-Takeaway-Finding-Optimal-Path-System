package algorithm;

import model.*;
import java.util.*;

public class AntColony implements OptimizationAlgorithm {
    private final int antCount;
    private final double pheromoneWeight;
    private final double evaporationRate;
    private final Random random = new Random();
    
    public AntColony(Map<String, Object> parameters) {
        this.antCount = (int) parameters.getOrDefault("antCount", 50);
        this.pheromoneWeight = (double) parameters.getOrDefault("pheromoneWeight", 1.0);
        this.evaporationRate = (double) parameters.getOrDefault("evaporationRate", 0.1);
    }
    
    @Override
    public Solution solve(Problem problem) {
        double[][] pheromones = initializePheromones(problem);
        Solution bestSolution = null;
        double bestCost = Double.MAX_VALUE;
        
        for (int iteration = 0; iteration < 1000; iteration++) {
            List<Solution> antSolutions = new ArrayList<>();
            
            // 每只蚂蚁构建解
            for (int ant = 0; ant < antCount; ant++) {
                Solution solution = constructSolution(problem, pheromones);
                antSolutions.add(solution);
                
                double cost = calculateCost(solution, problem);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestSolution = solution.clone();
                }
            }
            
            // 更新信息素
            updatePheromones(pheromones, antSolutions, problem);
        }
        
        return bestSolution;
    }
    
    @Override
    public String getName() {
        return "蚁群算法(ACO)";
    }
    
    private double[][] initializePheromones(Problem problem) {
        int customerCount = problem.getCustomers().size();
        double[][] pheromones = new double[customerCount][];
        for (int i = 0; i < customerCount; i++) {
            int pathCount = problem.getPaths().get("customer_" + i).size();
            pheromones[i] = new double[pathCount];
            Arrays.fill(pheromones[i], 1.0); // 初始信息素浓度为1
        }
        return pheromones;
    }
    
    private Solution constructSolution(Problem problem, double[][] pheromones) {
        Solution solution = new Solution(problem.getCustomers().size());
        
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            int pathCount = problem.getPaths().get("customer_" + i).size();
            double[] probabilities = new double[pathCount];
            double total = 0;
            
            // 计算每条路径的选择概率
            for (int j = 0; j < pathCount; j++) {
                Path path = problem.getPaths().get("customer_" + i).get(j);
                double heuristic = 1.0 / (path.getDistance() + path.getCost());
                probabilities[j] = Math.pow(pheromones[i][j], pheromoneWeight) * heuristic;
                total += probabilities[j];
            }
            
            // 轮盘赌选择路径
            double r = random.nextDouble() * total;
            double sum = 0;
            for (int j = 0; j < pathCount; j++) {
                sum += probabilities[j];
                if (sum >= r) {
                    solution.setPathIndex(i, j);
                    break;
                }
            }
        }
        
        updateSolutionMetrics(solution, problem);
        return solution;
    }
    
    private void updatePheromones(double[][] pheromones, List<Solution> solutions, Problem problem) {
        // 1. 信息素蒸发
        for (int i = 0; i < pheromones.length; i++) {
            for (int j = 0; j < pheromones[i].length; j++) {
                pheromones[i][j] *= (1 - evaporationRate);
            }
        }
        
        // 2. 信息素沉积
        for (Solution solution : solutions) {
            // contribution越小，说明成本越高（包括时间惩罚）
            double contribution = 1.0 / calculateCost(solution, problem);
            
            // 为解决方案使用的每条路径增加信息素
            for (int i = 0; i < solution.getPathIndices().length; i++) {
                pheromones[i][solution.getPathIndices()[i]] += contribution;
            }
        }
    }
    
    private double calculateCost(Solution solution, Problem problem) {
        double totalCost = 0;
        double totalTime = 0;
        
        // 1. 计算基础成本和时间
        for (int i = 0; i < solution.getPathIndices().length; i++) {
            Path path = problem.getPaths().get("customer_" + i).get(solution.getPathIndices()[i]);
            totalCost += path.getDistance() + path.getCost();  // 基础成本 = 距离 + 成本
            totalTime += path.getTime();                       // 累计时间
        }
        
        // 2. 如果超时，添加惩罚成本
        if (totalTime > problem.getTimeConstraint()) {
            totalCost += (totalTime - problem.getTimeConstraint()) * 1000;  // 惩罚因子为1000
        }
        
        return totalCost;  // 返回总成本（包括可能的时间惩罚）
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