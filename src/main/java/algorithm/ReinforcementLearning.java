package algorithm;

import model.*;
import java.util.*;

public class ReinforcementLearning implements OptimizationAlgorithm {
    private final int episodes;
    private final double learningRate;
    private final double discountFactor;
    private final double epsilon;
    private final Random random = new Random();
    private Map<String, double[]> qTable;
    
    public ReinforcementLearning(Map<String, Object> parameters) {
        this.episodes = (int) parameters.getOrDefault("episodes", 1000);
        this.learningRate = (double) parameters.getOrDefault("learningRate", 0.1);
        this.discountFactor = (double) parameters.getOrDefault("discountFactor", 0.9);
        this.epsilon = (double) parameters.getOrDefault("epsilon", 0.1);
        this.qTable = new HashMap<>();
    }
    
    @Override
    public Solution solve(Problem problem) {
        initializeQTable(problem);
        Solution bestSolution = generateInitialSolution(problem);
        double bestReward = calculateReward(bestSolution, problem);
        
        // Training phase
        for (int episode = 0; episode < episodes; episode++) {
            Solution solution = runEpisode(problem);
            double reward = calculateReward(solution, problem);
            
            if (reward > bestReward) {
                bestReward = reward;
                bestSolution = solution.clone();
            }
            
            // 如果找到了可行解就提前结束
            if (solution.getTotalTime() <= problem.getTimeConstraint()) {
                bestSolution = solution.clone();
                break;
            }
        }
        
        // 如果没有找到可行解，生成一个贪心解
        if (bestSolution.getTotalTime() > problem.getTimeConstraint()) {
            Solution greedySolution = generateGreedySolution(problem);
            if (greedySolution.getTotalTime() <= problem.getTimeConstraint()) {
                bestSolution = greedySolution;
            }
        }
        
        return bestSolution;
    }
    
    private Solution generateInitialSolution(Problem problem) {
        Solution solution = new Solution(problem.getCustomers().size());
        double totalTime = 0;
        
        // 对每个顾客选择时间最短的路径
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            List<Path> paths = problem.getPaths().get("customer_" + i);
            int bestPathIndex = 0;
            double minTime = Double.MAX_VALUE;
            
            for (int j = 0; j < paths.size(); j++) {
                if (paths.get(j).getTime() < minTime) {
                    minTime = paths.get(j).getTime();
                    bestPathIndex = j;
                }
            }
            
            solution.setPathIndex(i, bestPathIndex);
            totalTime += minTime;
        }
        
        updateSolutionMetrics(solution, problem);
        return solution;
    }
    
    private Solution generateGreedySolution(Problem problem) {
        Solution solution = new Solution(problem.getCustomers().size());
        double totalTime = 0;
        
        // 对每个顾客选择满足时间约束且成本最低的路径
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            List<Path> paths = problem.getPaths().get("customer_" + i);
            int bestPathIndex = 0;
            double minCost = Double.MAX_VALUE;
            
            for (int j = 0; j < paths.size(); j++) {
                Path path = paths.get(j);
                if (totalTime + path.getTime() <= problem.getTimeConstraint() * 1.1) {
                    double cost = path.getDistance() + path.getCost();
                    if (cost < minCost) {
                        minCost = cost;
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
    
    @Override
    public String getName() {
        return "强化学习算法(RL)";
    }
    
    private void initializeQTable(Problem problem) {
        qTable.clear();
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            for (int j = 0; j < problem.getPaths().get("customer_" + i).size(); j++) {
                String state = getState(i);
                if (!qTable.containsKey(state)) {
                    qTable.put(state, new double[problem.getPaths().get("customer_" + i).size()]);
                }
            }
        }
    }
    
    private Solution runEpisode(Problem problem) {
        Solution solution = new Solution(problem.getCustomers().size());
        double currentTime = 0.0;
        
        for (int customerIndex = 0; customerIndex < problem.getCustomers().size(); customerIndex++) {
            String state = getState(customerIndex);
            int action = selectAction(state, problem.getPaths().get("customer_" + customerIndex).size());
            
            // Execute action
            solution.setPathIndex(customerIndex, action);
            Path path = problem.getPaths().get("customer_" + customerIndex).get(action);
            currentTime += path.getTime();
            
            // Update Q-value
            double reward = calculateStepReward(path, currentTime, problem.getTimeConstraint());
            String nextState = getState(customerIndex + 1);
            
            if (qTable.containsKey(nextState)) {
                double maxNextQ = Arrays.stream(qTable.get(nextState)).max().orElse(0.0);
                double oldQ = qTable.get(state)[action];
                double newQ = oldQ + learningRate * (reward + discountFactor * maxNextQ - oldQ);
                qTable.get(state)[action] = newQ;
            }
        }
        
        updateSolutionMetrics(solution, problem);
        return solution;
    }
    
    private int selectAction(String state, int actionCount) {
        if (random.nextDouble() < epsilon) {
            return random.nextInt(actionCount);
        }
        
        double[] qValues = qTable.get(state);
        int bestAction = 0;
        double bestValue = qValues[0];
        
        for (int i = 1; i < qValues.length; i++) {
            if (qValues[i] > bestValue) {
                bestValue = qValues[i];
                bestAction = i;
            }
        }
        
        return bestAction;
    }
    
    private String getState(int customerIndex) {
        return String.valueOf(customerIndex);
    }
    
    private double calculateStepReward(Path path, double currentTime, double timeConstraint) {
        double reward = -(path.getDistance() + path.getCost());
        if (currentTime > timeConstraint) {
            reward -= (currentTime - timeConstraint) * 100;
        }
        return reward;
    }
    
    private double calculateReward(Solution solution, Problem problem) {
        double totalCost = solution.getTotalCost();
        double totalTime = solution.getTotalTime();
        
        if (totalTime > problem.getTimeConstraint()) {
            return Double.NEGATIVE_INFINITY;
        }
        
        return -totalCost;
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