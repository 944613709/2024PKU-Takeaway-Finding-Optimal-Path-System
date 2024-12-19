package util;

import model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.*;

public class DataGenerator {
    private static final Random random = new Random();
    
    public static void generateAndSaveTestData(int restaurantCount, int customerCount, int pathsPerCustomer, String filename) {
        try {
            // 生成数据
            Map<String, Object> data = new HashMap<>();
            
            // 生成餐厅数据
            List<Restaurant> restaurants = new ArrayList<>();
            for (int i = 0; i < restaurantCount; i++) {
                restaurants.add(new Restaurant(i));
            }
            data.put("restaurants", restaurants);
            
            // 生成顾客数据
            List<Customer> customers = new ArrayList<>();
            for (int i = 0; i < customerCount; i++) {
                customers.add(new Customer(i));
            }
            data.put("customers", customers);
            
            // 为每个顾客生成多条可选路径
            Map<String, List<Path>> paths = new HashMap<>();
            for (int i = 0; i < customerCount; i++) {
                List<Path> customerPaths = new ArrayList<>();
                for (int j = 0; j < pathsPerCustomer; j++) {
                    // 生成随机路径属性
                    double distance = 1 + random.nextDouble() * 9; // 1-10公里
                    double costFactor = 0.8 + random.nextDouble() * 0.4; // 0.8-1.2的随机系数
                    double cost = distance * costFactor;
                    double timePerKm = 3 + random.nextDouble() * 2; // 3-5分钟/公里
                    double time = distance * timePerKm;
                    
                    customerPaths.add(new Path(distance, cost, time));
                }
                paths.put("customer_" + i, customerPaths);
            }
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
} 