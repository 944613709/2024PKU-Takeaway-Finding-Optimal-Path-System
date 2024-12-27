package experiment;

import algorithm.*;
import model.*;
import util.DataGenerator;
import java.util.*;

public class ExperimentRunner {
    private static final int REPEAT_TIMES = 10;
    private static final String[] ALGORITHMS = {"GA", "SA", "ACO", "TS", "DP", "RL"};
    
    public static void main(String[] args) {
        // 1. 有效性实验数据集
        System.out.println("\n=== 有效性实验 ===");
        Map<String, Object> effectivenessData = generateTestData("effectiveness_test.json");
        Problem effectivenessProblem = createProblem(effectivenessData);
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n使用 %s:\n", algorithmType);
            runEffectivenessExperiment(effectivenessProblem, algorithmType);
        }
        
        // 2. 效率实验数据集
        System.out.println("\n=== 效率实验 ===");
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
        Map<String, Object> stabilityData = generateTestData("stability_test.json");
        Problem stabilityProblem = createProblem(stabilityData);
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n使用 %s:\n", algorithmType);
            runStabilityExperiment(stabilityProblem, algorithmType);
        }
        
        // 4. 适应性实验数据集
        System.out.println("\n=== 适应性实验 ===");
        Map<String, Object> adaptabilityData = generateTestData("adaptability_test.json");
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n使用 %s:\n", algorithmType);
            runAdaptabilityExperiment(algorithmType, adaptabilityData);
        }
        
        // 5. 算法对比实验
        System.out.println("\n=== 算法对比实验 ===");
        Map<String, Object> comparisonData = generateTestData("comparison_test.json");
        Problem comparisonProblem = createProblem(comparisonData);
        runComparisonExperiment(comparisonProblem);
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
    
    private static void runEffectivenessExperiment(Problem problem, String algorithmType) {
        OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
        Solution solution = algorithm.solve(problem);
        
        System.out.printf("总成本: %.2f\n", solution.getTotalCost());
        System.out.printf("总时间: %.2f 分钟\n", solution.getTotalTime());
        System.out.printf("是否满足时间约束: %s\n", 
            solution.getTotalTime() <= problem.getTimeConstraint() ? "是" : "否");
    }
    
    private static void runEfficiencyExperiment(Problem problem, String algorithmType, int size) {
        long startTime = System.currentTimeMillis();
        OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
        Solution solution = algorithm.solve(problem);
        long endTime = System.currentTimeMillis();
        
        System.out.printf("%s: 耗时 %d ms, 成本 %.2f\n",
            algorithmType, (endTime - startTime), solution.getTotalCost());
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
        
        System.out.printf("成本: 平均值=%.2f, 方差=%.2f\n",
            calculateMean(costs), calculateVariance(costs));
        System.out.printf("时间: 平均值=%.2f, 方差=%.2f\n",
            calculateMean(times), calculateVariance(times));
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
            
            System.out.printf("时间约束 %.1f: 实际时间=%.1f, 成本=%.2f\n",
                constraint, solution.getTotalTime(), solution.getTotalCost());
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
        }
        
        System.out.println("\n各算法性能对比:");
        for (Map.Entry<String, List<Double>> entry : results.entrySet()) {
            System.out.printf("%s: 平均成本=%.2f, 方差=%.2f\n",
                entry.getKey(),
                calculateMean(entry.getValue()),
                calculateVariance(entry.getValue()));
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