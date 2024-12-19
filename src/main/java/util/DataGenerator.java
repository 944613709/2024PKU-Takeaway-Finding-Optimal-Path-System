package util;

import com.google.gson.Gson;
import model.Customer;
import model.Path;
import model.Restaurant;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataGenerator {
    private static final Random random = new Random();
    private static final double MIN_LAT = 31.2;  // 上海市经纬度范围
    private static final double MAX_LAT = 31.4;
    private static final double MIN_LON = 121.3;
    private static final double MAX_LON = 121.5;
    
    public static void generateAndSaveTestData(int numRestaurants, int numCustomers, 
                                             int pathsPerCustomer, String fileName) {
        Map<String, Object> data = new HashMap<>();
        
        // 生成餐厅数据
        List<Restaurant> restaurants = new ArrayList<>();
        for (int i = 0; i < numRestaurants; i++) {
            restaurants.add(new Restaurant(i,
                MIN_LAT + random.nextDouble() * (MAX_LAT - MIN_LAT),
                MIN_LON + random.nextDouble() * (MAX_LON - MIN_LON)
            ));
        }
        
        // 生成顾客数据
        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < numCustomers; i++) {
            customers.add(new Customer(i,
                MIN_LAT + random.nextDouble() * (MAX_LAT - MIN_LAT),
                MIN_LON + random.nextDouble() * (MAX_LON - MIN_LON)
            ));
        }
        
        // 生成路径数据
        Map<String, List<Path>> paths = new HashMap<>();
        for (Customer customer : customers) {
            List<Path> customerPaths = new ArrayList<>();
            for (int i = 0; i < pathsPerCustomer; i++) {
                double distance = 1 + random.nextDouble() * 10; // 1-10公里
                double cost = distance * (0.8 + random.nextDouble() * 0.4); // 基于距离的成本
                double time = distance * (3 + random.nextDouble() * 2); // 基于距离的时间
                customerPaths.add(new Path(distance, cost, time));
            }
            paths.put("customer_" + customer.getId(), customerPaths);
        }
        
        data.put("restaurants", restaurants);
        data.put("customers", customers);
        data.put("paths", paths);
        
        // 保存到JSON文件
        try (FileWriter writer = new FileWriter(fileName)) {
            new Gson().toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Map<String, Object> loadTestData(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            Gson gson = new Gson();
            Map<String, Object> rawData = gson.fromJson(reader, Map.class);
            Map<String, Object> convertedData = new HashMap<>();
            
            // 转换餐厅数据
            List<Map<String, Object>> rawRestaurants = (List<Map<String, Object>>) rawData.get("restaurants");
            List<Restaurant> restaurants = rawRestaurants.stream()
                .map(map -> new Restaurant(
                    ((Double) map.get("id")).intValue(),
                    (Double) map.get("latitude"),
                    (Double) map.get("longitude")))
                .collect(Collectors.toList());
            
            // 转换顾客数据
            List<Map<String, Object>> rawCustomers = (List<Map<String, Object>>) rawData.get("customers");
            List<Customer> customers = rawCustomers.stream()
                .map(map -> new Customer(
                    ((Double) map.get("id")).intValue(),
                    (Double) map.get("latitude"),
                    (Double) map.get("longitude")))
                .collect(Collectors.toList());
            
            // 转换路径数据
            Map<String, List<Map<String, Object>>> rawPaths = (Map<String, List<Map<String, Object>>>) rawData.get("paths");
            Map<String, List<Path>> paths = new HashMap<>();
            
            rawPaths.forEach((key, value) -> {
                List<Path> pathList = value.stream()
                    .map(map -> new Path(
                        (Double) map.get("distance"),
                        (Double) map.get("cost"),
                        (Double) map.get("time")))
                    .collect(Collectors.toList());
                paths.put(key, pathList);
            });
            
            convertedData.put("restaurants", restaurants);
            convertedData.put("customers", customers);
            convertedData.put("paths", paths);
            
            return convertedData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
} 