package experiment;

import algorithm.*;
import model.*;
import util.DataGenerator;
import java.util.*;

public class ExperimentRunner {
    private static final int REPEAT_TIMES = 10;
    private static final String[] ALGORITHMS = {"GA", "SA", "ACO", "TS"};
    
    public static void main(String[] args) {
        // 生成测试数据
        Map<String, Object> data = generateTestData();
        Problem problem = createProblem(data);
        
        // 运行所有实验
        for (String algorithmType : ALGORITHMS) {
            System.out.printf("\n=== 使用 %s 进行实验 ===\n", algorithmType);
            
            // 1. 有效性实验
            runEffectivenessExperiment(problem, algorithmType);
            
            // 2. 效率实验
            runEfficiencyExperiment(algorithmType);
            
            // 3. 稳定性实验
            runStabilityExperiment(problem, algorithmType);
            
            // 4. 适应性实验
            runAdaptabilityExperiment(algorithmType);
        }
        
        // 算法对比实验
        runComparisonExperiment();
    }
    
    private static Map<String, Object> generateTestData() {
        String dataFile = "test_data.json";
        DataGenerator.generateAndSaveTestData(10, 20, 5, dataFile);
        return DataGenerator.loadTestData(dataFile);
    }
    
    private static Problem createProblem(Map<String, Object> data) {
        return new Problem(
            (List<Customer>) data.get("customers"),
            (Map<String, List<Path>>) data.get("paths"),
            120.0
        );
    }
    
    private static void runEffectivenessExperiment(Problem problem, String algorithmType) {
        System.out.println("\n1. 算法有效性实验");
        OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
        Solution solution = algorithm.solve(problem);
        
        System.out.printf("总成本: %.2f\n", solution.getTotalCost());
        System.out.printf("总时间: %.2f 分钟\n", solution.getTotalTime());
        System.out.printf("是否满足时间约束: %s\n", 
            solution.getTotalTime() <= problem.getTimeConstraint() ? "是" : "否");
    }
    
    private static void runEfficiencyExperiment(String algorithmType) {
        System.out.println("\n2. 算法效率实验");
        int[] sizes = {10, 20, 50, 100};
        
        for (int size : sizes) {
            String dataFile = String.format("efficiency_test_%d.json", size);
            DataGenerator.generateAndSaveTestData(size/2, size, 5, dataFile);
            Map<String, Object> data = DataGenerator.loadTestData(dataFile);
            Problem problem = createProblem(data);
            
            long startTime = System.currentTimeMillis();
            OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, new HashMap<>());
            Solution solution = algorithm.solve(problem);
            long endTime = System.currentTimeMillis();
            
            System.out.printf("规模 %d: 耗时 %d ms, 成本 %.2f\n",
                size, (endTime - startTime), solution.getTotalCost());
        }
    }
    
    private static void runStabilityExperiment(Problem problem, String algorithmType) {
        System.out.println("\n3. 算法稳定性实验");
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
    
    private static void runAdaptabilityExperiment(String algorithmType) {
        System.out.println("\n4. 算法适应性实验");
        double[] timeConstraints = {60.0, 90.0, 120.0, 150.0, 180.0};
        
        for (double constraint : timeConstraints) {
            Map<String, Object> data = generateTestData();
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
    
    private static void runComparisonExperiment() {
        System.out.println("\n=== 算法对比实验 ===");
        Problem problem = createProblem(generateTestData());
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
        
        // 输出对比结果
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