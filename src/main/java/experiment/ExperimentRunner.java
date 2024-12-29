package experiment;

import algorithm.*;
import model.*;
import util.*;
import java.util.*;

public class ExperimentRunner {
    private static final int REPEAT_TIMES = 30;
    private static final String[] ALGORITHMS = {"GA", "SA", "ACO", "TS", "RL", "BK"};
    
    public static void main(String[] args) {
        // 初始化实验记录器
        ExperimentLogger.initializeExperiment("实验结果");
        
        // 1. 有效性实验数据集
        System.out.println("\n=== 有效性实验 ===");
        ExperimentLogger.addSheet("有效性实验");
        Map<String, Object> effectivenessData = generateTestData("effectiveness_test.json", 5, 30, 5); // 使用较小的数据集以便回溯算法能在合理时间内找到最优解
        Problem effectivenessProblem = createProblem(effectivenessData);
        
        // 首先使用回溯算法找到最优解
        System.out.println("\n使用回溯算法寻找最优解:");
        OptimizationAlgorithm backtracking = AlgorithmFactory.createAlgorithm("BK", new HashMap<>());
        Solution optimalSolution = backtracking.solve(effectivenessProblem);
        System.out.printf("最优解成本: %.2f\n", optimalSolution.getTotalCost());
        System.out.printf("最优解时间: %.2f 分钟\n", optimalSolution.getTotalTime());
        
        // 然后测试其他算法
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n使用 %s:\n", algorithmType);
            runEffectivenessExperiment(effectivenessProblem, algorithmType, optimalSolution);
        }
        
        // 2. 效率实验数据集
        System.out.println("\n=== 效率实验 ===");
        ExperimentLogger.addSheet("效率实验");
        int[] sizes = {10, 20, 50, 100};
        for (int size : sizes) {
            String dataFile = String.format("efficiency_test_%d.json", size);
            Map<String, Object> efficiencyData = generateTestData(dataFile, size/2, size, 5);
            Problem efficiencyProblem = createProblem(efficiencyData);
            System.out.printf("\n规模 %d:\n", size);
            for (String algorithmType : ALGORITHMS) {
                runEfficiencyExperiment(efficiencyProblem, algorithmType, size);
            }
        }
        
        // 3. 稳定性实验数据集
        System.out.println("\n=== 稳定性实验 ===");
        ExperimentLogger.addSheet("稳定性实验");
        Map<String, Object> stabilityData = generateTestData("stability_test.json");
        Problem stabilityProblem = createProblem(stabilityData);
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n使用 %s:\n", algorithmType);
            runStabilityExperiment(stabilityProblem, algorithmType);
        }
        
        // 4. 适应性实验数据集
        System.out.println("\n=== 适应性实验 ===");
        ExperimentLogger.addSheet("适应性实验");
        Map<String, Object> adaptabilityData = generateTestData("adaptability_test.json");
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n使用 %s:\n", algorithmType);
            runAdaptabilityExperiment(algorithmType, adaptabilityData);
        }
        
        // 5. 算法对比实验
        System.out.println("\n=== 算法对比实验 ===");
        ExperimentLogger.addSheet("算法对比实验");
        Map<String, Object> comparisonData = generateTestData("comparison_test.json");
        Problem comparisonProblem = createProblem(comparisonData);
        runComparisonExperiment(comparisonProblem);
        
        // 保存所有实验结果
        ExperimentLogger.saveResults();
    }
    
    private static Map<String, Object> generateTestData(String filename) {
        return generateTestData(filename, 10, 20, 5);
    }
    
    private static Map<String, Object> generateTestData(String filename, int restaurantCount, 
                                                      int customerCount, int pathsPerCustomer) {
        DataGenerator.generateAndSaveTestData(restaurantCount, customerCount, pathsPerCustomer, filename);
        return DataGenerator.loadTestData(filename);
    }
    
    private static Problem createProblem(Map<String, Object> data) {
        return new Problem(
            (List<Customer>) data.get("customers"),
            (Map<String, List<Path>>) data.get("paths"),
            120.0
        );
    }
    
    private static void runEffectivenessExperiment(Problem problem, String algorithmType, Solution optimalSolution) {
        OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
        Solution solution = algorithm.solve(problem);
        
        // 计算与最优解的差距
        double costGap = ((solution.getTotalCost() - optimalSolution.getTotalCost()) / optimalSolution.getTotalCost()) * 100;
        double timeGap = ((solution.getTotalTime() - optimalSolution.getTotalTime()) / optimalSolution.getTotalTime()) * 100;
        
        // 控制台输出
        System.out.printf("总成本: %.2f (与最优解差距: %.2f%%)\n", 
            solution.getTotalCost(), costGap);
        System.out.printf("总时间: %.2f 分钟 (与最优解差距: %.2f%%)\n", 
            solution.getTotalTime(), timeGap);
        System.out.printf("是否满足时间约束: %s\n", 
            solution.getTotalTime() <= problem.getTimeConstraint() ? "是" : "否");
            
        // Excel记录
        Map<String, Object> result = new HashMap<>();
        result.put("算法类型", algorithmType);
        result.put("总成本", solution.getTotalCost());
        result.put("最优解成本", optimalSolution.getTotalCost());
        result.put("成本差距(%)", costGap);
        result.put("总时间(分钟)", solution.getTotalTime());
        result.put("最优解时间(分钟)", optimalSolution.getTotalTime());
        result.put("时间差距(%)", timeGap);
        result.put("是否满足时间约束", solution.getTotalTime() <= problem.getTimeConstraint() ? "是" : "否");
        ExperimentLogger.logResult(result);
    }
    
    private static void runEfficiencyExperiment(Problem problem, String algorithmType, int size) {
        long startTime = System.currentTimeMillis();
        OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
        Solution solution = algorithm.solve(problem);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 控制台输出
        System.out.printf("%s: 耗时 %d ms, 成本 %.2f\n",
            algorithmType, duration, solution.getTotalCost());
            
        // Excel记录
        Map<String, Object> result = new HashMap<>();
        result.put("问题规模", size);
        result.put("算法类型", algorithmType);
        result.put("耗时(ms)", duration);
        result.put("总成本", solution.getTotalCost());
        ExperimentLogger.logResult(result);
    }
    
    private static void runStabilityExperiment(Problem problem, String algorithmType) {
        List<Double> costs = new ArrayList<>();
        List<Double> times = new ArrayList<>();
        
        for (int i = 0; i < REPEAT_TIMES; i++) {
            OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
            Solution solution = algorithm.solve(problem);
            costs.add(solution.getTotalCost());
            times.add(solution.getTotalTime());
        }
        
        double costMean = calculateMean(costs);
        double costVariance = calculateVariance(costs);
        double timeMean = calculateMean(times);
        double timeVariance = calculateVariance(times);
        
        // 控制台输出
        System.out.printf("成本: 平均值=%.2f, 方差=%.2f\n", costMean, costVariance);
        System.out.printf("时间: 平均值=%.2f, 方差=%.2f\n", timeMean, timeVariance);
        
        // Excel记录
        Map<String, Object> result = new HashMap<>();
        result.put("算法类型", algorithmType);
        result.put("成本平均值", costMean);
        result.put("成本方差", costVariance);
        result.put("时间平均值", timeMean);
        result.put("时间方差", timeVariance);
        ExperimentLogger.logResult(result);
    }
    
    private static void runAdaptabilityExperiment(String algorithmType, Map<String, Object> data) {
        double[] timeConstraints = {60.0, 90.0, 120.0, 150.0, 180.0};
        
        for (double constraint : timeConstraints) {
            Problem problem = new Problem(
                (List<Customer>) data.get("customers"),
                (Map<String, List<Path>>) data.get("paths"),
                constraint
            );
            
            OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
            Solution solution = algorithm.solve(problem);
            
            // 控制台输出
            System.out.printf("时间约束 %.1f: 实际时间=%.1f, 成本=%.2f\n",
                constraint, solution.getTotalTime(), solution.getTotalCost());
                
            // Excel记录
            Map<String, Object> result = new HashMap<>();
            result.put("算法类型", algorithmType);
            result.put("时间约束", constraint);
            result.put("实际时间", solution.getTotalTime());
            result.put("总成本", solution.getTotalCost());
            ExperimentLogger.logResult(result);
        }
    }
    
    private static void runComparisonExperiment(Problem problem) {
        Map<String, List<Double>> results = new HashMap<>();
        
        for (String algorithmType : ALGORITHMS) {
            List<Double> costs = new ArrayList<>();
            for (int i = 0; i < REPEAT_TIMES; i++) {
                OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
                Solution solution = algorithm.solve(problem);
                costs.add(solution.getTotalCost());
            }
            results.put(algorithmType, costs);
            
            double meanCost = calculateMean(costs);
            double varianceCost = calculateVariance(costs);
            
            // 控制台输出
            System.out.printf("%s: 平均成本=%.2f, 方差=%.2f\n",
                algorithmType, meanCost, varianceCost);
                
            // Excel记录
            Map<String, Object> result = new HashMap<>();
            result.put("算法类型", algorithmType);
            result.put("平均成本", meanCost);
            result.put("成本方差", varianceCost);
            ExperimentLogger.logResult(result);
        }
    }
    
    private static double calculateMean(List<Double> numbers) {
        return numbers.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
    
    private static double calculateVariance(List<Double> numbers) {
        double mean = calculateMean(numbers);
        return numbers.stream()
            .mapToDouble(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
    }
} 