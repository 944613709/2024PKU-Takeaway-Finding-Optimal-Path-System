import util.DataGenerator;
import algorithm.*;
import model.*;
import java.util.*;
import java.io.*;
import org.yaml.snakeyaml.Yaml;

public class Main {
    public static void main(String[] args) {
        // 解析命令行参数或配置文件
        String algorithmType = parseAlgorithmType(args);
        Map<String, Object> parameters = loadParameters();
        
        System.out.println("外卖配送路径优化系统启动...");
        
        // 1. 生成测试数据
        String dataFile = "delivery_data.json";
        System.out.println("正在生成测试数据...");
        DataGenerator.generateAndSaveTestData(10, 20, 5, dataFile);
        
        // 2. 加载测试数据
        System.out.println("正在加载测试数据...");
        Map<String, Object> data = DataGenerator.loadTestData(dataFile);
        if (data == null) {
            System.out.println("数据加载失败！");
            return;
        }
        
        // 3. 创建问题实例
        Problem problem = new Problem(
            (List<Customer>) data.get("customers"),
            (Map<String, List<Path>>) data.get("paths"),
            120.0  // 2小时时间约束
        );
        
        // 4. 创建算法实例
        OptimizationAlgorithm algorithm = AlgorithmFactory.createAlgorithm(algorithmType, parameters);
        System.out.printf("使用 %s 求解...\n", algorithm.getName());
        
        // 5. 运行算法求解
        long startTime = System.currentTimeMillis();
        Solution solution = algorithm.solve(problem);
        long endTime = System.currentTimeMillis();
        
        // 6. 输出结果
        if (solution != null) {
            System.out.println("\n求解完成！");
            System.out.println("总配送成本: " + solution.getTotalCost());
            System.out.println("总配送时间: " + solution.getTotalTime() + " 分钟");
            System.out.println("求解耗时: " + (endTime - startTime) + " 毫秒");
            
            System.out.println("\n配送路径详情:");
            int[] pathIndices = solution.getPathIndices();
            for (int i = 0; i < pathIndices.length; i++) {
                System.out.printf("顾客 %d: 选择路径 %d\n", i, pathIndices[i]);
            }
        } else {
            System.out.println("未能找到可行解！");
        }
    }
    
    private static String parseAlgorithmType(String[] args) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals("--algorithm")) {
                return args[i + 1];
            }
        }
        return "GA"; // 默认使用遗传算法
    }
    
    private static Map<String, Object> loadParameters() {
        try (InputStream input = new FileInputStream("config.yml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);
            return (Map<String, Object>) ((Map<String, Object>) config.get("algorithm")).get("parameters");
        } catch (Exception e) {
            System.out.println("使用默认参数配置");
            return new HashMap<>();
        }
    }
} 