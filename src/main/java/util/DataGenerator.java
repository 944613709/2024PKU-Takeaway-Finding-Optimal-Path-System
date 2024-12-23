package util;

import model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;
import org.yaml.snakeyaml.Yaml;

public class DataGenerator {
    private static final Random random = new Random();
    
    public static void generateAndSaveTestData(int restaurantCount, int customerCount, int pathsPerCustomer, String filename) {
        try {
            Map<String, Object> config = loadConfig();
            String strategy = (String) config.getOrDefault("strategy", "B");
            
            Map<String, List<Path>> paths;
            if (strategy.equals("A")) {
                paths = generateRandomPaths(customerCount, pathsPerCustomer);
            } else {
                paths = generateFeasiblePaths(customerCount, pathsPerCustomer, 120.0);
            }
            
            Map<String, Object> data = new HashMap<>();
            
            // 生成餐厅和顾客数据保持不变
            List<Restaurant> restaurants = new ArrayList<>();
            for (int i = 0; i < restaurantCount; i++) {
                restaurants.add(new Restaurant(i));
            }
            data.put("restaurants", restaurants);
            
            List<Customer> customers = new ArrayList<>();
            for (int i = 0; i < customerCount; i++) {
                customers.add(new Customer(i));
            }
            data.put("customers", customers);
            
            data.put("paths", paths);
            
            // 保存到JSON文件
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(filename)) {
                gson.toJson(data, writer);
            }
            
        } catch (IOException e) {
            System.err.println("Error generating test data: " + e.getMessage());
        }
    }
    
    private static Map<String, List<Path>> generateFeasiblePaths(int customerCount, int pathsPerCustomer, double timeConstraint) {
        Map<String, List<Path>> paths = new HashMap<>();
        
        // 首先生成一个确保可行的解
        double remainingTime = timeConstraint;
        double[] feasibleTimes = new double[customerCount];
        
        // 为每个顾客分配合理的时间
        for (int i = 0; i < customerCount - 1; i++) {
            double maxTime = remainingTime * 0.5; // 留出足够的时间给其他顾客
            feasibleTimes[i] = 2 + random.nextDouble() * Math.min(maxTime - 2, 8); // 2-10分钟之间
            remainingTime -= feasibleTimes[i];
        }
        feasibleTimes[customerCount - 1] = remainingTime;
        
        // 为每个顾客生成路径，确保包含可行路径
        for (int i = 0; i < customerCount; i++) {
            List<Path> customerPaths = new ArrayList<>();
            
            // 首先添加一条可行的路径
            double feasibleDistance = feasibleTimes[i] / 4.0; // 假设平均速度为4分钟/公里
            double feasibleCost = feasibleDistance * (0.8 + random.nextDouble() * 0.4);
            customerPaths.add(new Path(feasibleDistance, feasibleCost, feasibleTimes[i]));
            
            // 然后生成其他随机路径
            for (int j = 1; j < pathsPerCustomer; j++) {
                double distance = 1 + random.nextDouble() * 9; // 1-10公里
                double costFactor = 0.8 + random.nextDouble() * 0.4; // 0.8-1.2的随机系数
                double cost = distance * costFactor;
                double timePerKm = 3 + random.nextDouble() * 2; // 3-5分钟/公里
                double time = distance * timePerKm;
                
                customerPaths.add(new Path(distance, cost, time));
            }
            
            // 随机打乱路径顺序
            Collections.shuffle(customerPaths);
            paths.put("customer_" + i, customerPaths);
        }
        
        return paths;
    }
    
    private static Map<String, List<Path>> generateRandomPaths(int customerCount, int pathsPerCustomer) {
        Map<String, List<Path>> paths = new HashMap<>();
        for (int i = 0; i < customerCount; i++) {
            List<Path> customerPaths = new ArrayList<>();
            for (int j = 0; j < pathsPerCustomer; j++) {
                double distance = 1 + random.nextDouble() * 9;
                double costFactor = 0.8 + random.nextDouble() * 0.4;
                double cost = distance * costFactor;
                double timePerKm = 3 + random.nextDouble() * 2;
                double time = distance * timePerKm;
                
                customerPaths.add(new Path(distance, cost, time));
            }
            paths.put("customer_" + i, customerPaths);
        }
        return paths;
    }
    
    // 验证数据集是否包含可行解
    public static boolean validateDataset(Map<String, Object> data, double timeConstraint) {
        Map<String, List<Path>> paths = (Map<String, List<Path>>) data.get("paths");
        int customerCount = ((List<Customer>) data.get("customers")).size();
        
        // 尝试找到一个可行解
        for (int attempt = 0; attempt < 1000; attempt++) {
            double totalTime = 0;
            boolean feasible = true;
            
            for (int i = 0; i < customerCount; i++) {
                List<Path> customerPaths = paths.get("customer_" + i);
                double minTime = Double.MAX_VALUE;
                
                // 找到该顾客的最短时间路径
                for (Path path : customerPaths) {
                    minTime = Math.min(minTime, path.getTime());
                }
                
                totalTime += minTime;
                if (totalTime > timeConstraint) {
                    feasible = false;
                    break;
                }
            }
            
            if (feasible) {
                return true;
            }
        }
        
        return false;
    }
    
    public static Map<String, Object> loadTestData(String filename) {
        try {
            Gson gson = new Gson();
            Map<String, Object> convertedData = new HashMap<>();
            
            try (FileReader reader = new FileReader(filename)) {
                Map<String, Object> rawData = gson.fromJson(reader, Map.class);
                
                // 转换餐厅数据
                List<Map<String, Object>> rawRestaurants = (List<Map<String, Object>>) rawData.get("restaurants");
                List<Restaurant> restaurants = new ArrayList<>();
                for (Map<String, Object> map : rawRestaurants) {
                    restaurants.add(new Restaurant(((Double) map.get("id")).intValue()));
                }
                convertedData.put("restaurants", restaurants);
                
                // 转换顾客数据
                List<Map<String, Object>> rawCustomers = (List<Map<String, Object>>) rawData.get("customers");
                List<Customer> customers = new ArrayList<>();
                for (Map<String, Object> map : rawCustomers) {
                    customers.add(new Customer(((Double) map.get("id")).intValue()));
                }
                convertedData.put("customers", customers);
                
                // 转换路径数据
                Map<String, List<Map<String, Object>>> rawPaths = (Map<String, List<Map<String, Object>>>) rawData.get("paths");
                Map<String, List<Path>> paths = new HashMap<>();
                
                for (Map.Entry<String, List<Map<String, Object>>> entry : rawPaths.entrySet()) {
                    List<Path> pathList = new ArrayList<>();
                    for (Map<String, Object> pathMap : entry.getValue()) {
                        pathList.add(new Path(
                            (Double) pathMap.get("distance"),
                            (Double) pathMap.get("cost"),
                            (Double) pathMap.get("time")
                        ));
                    }
                    paths.put(entry.getKey(), pathList);
                }
                convertedData.put("paths", paths);
                
                return convertedData;
            }
        } catch (IOException e) {
            System.err.println("Error loading test data: " + e.getMessage());
            return null;
        }
    }
    
    private static Map<String, Object> loadConfig() {
        try (InputStream input = new FileInputStream("config.yml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);
            return (Map<String, Object>) config.getOrDefault("dataGeneration", 
                new HashMap<String, Object>() {{
                    put("strategy", "B");
                    put("timeConstraint", 120.0);
                    put("minTimePerCustomer", 2.0);
                    put("maxTimePerCustomer", 10.0);
                    put("minDistance", 1.0);
                    put("maxDistance", 10.0);
                    put("costFactor", new HashMap<String, Double>() {{
                        put("min", 0.8);
                        put("max", 1.2);
                    }});
                }});
        } catch (Exception e) {
            System.out.println("使用默认数据生成配置");
            return new HashMap<String, Object>() {{
                put("strategy", "B");
                put("timeConstraint", 120.0);
                put("minTimePerCustomer", 2.0);
                put("maxTimePerCustomer", 10.0);
                put("minDistance", 1.0);
                put("maxDistance", 10.0);
                put("costFactor", new HashMap<String, Double>() {{
                    put("min", 0.8);
                    put("max", 1.2);
                }});
            }};
        }
    }
} 