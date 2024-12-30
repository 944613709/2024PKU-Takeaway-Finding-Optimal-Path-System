import java.util.Random;
import java.util.HashSet;
import java.util.Set;

public class TestDataSet {

    public static final int CUSTOMERS = 5; // 客户地点数量
    public static final int PATHS_PER_CUSTOMER = 3; // 每个客户地点的候选路径数量
    public static final int TIME_LIMIT = 150; // 时间限制
    private static final int DISCOUNT_MAX = 500; // 最大优惠值
    public static final int EXTRA_TIME_COST = 100; // 超时额外费用

    private int[][] distances;
    public int[][] costs;
    public int[][] times;
    public int[][][][] discounts;

    public TestDataSet() {
        generateTestData();
    }

    private void generateTestData() {
        Random random = new Random();
        distances = new int[CUSTOMERS][PATHS_PER_CUSTOMER];
        costs = new int[CUSTOMERS][PATHS_PER_CUSTOMER];
        times = new int[CUSTOMERS][PATHS_PER_CUSTOMER];
        discounts = new int[CUSTOMERS][CUSTOMERS][PATHS_PER_CUSTOMER][PATHS_PER_CUSTOMER];
        Set<String> usedPaths = new HashSet<>();

        // 生成路径属性
        for (int i = 0; i < CUSTOMERS; i++) {
            for (int j = 0; j < PATHS_PER_CUSTOMER; j++) {
                distances[i][j] = random.nextInt(100); // 随机生成距离
                costs[i][j] = random.nextInt(1000); // 随机生成费用
                times[i][j] = random.nextInt(60); // 随机生成耗时
            }
        }

        // 生成组合优惠
        for (int i = 0; i < CUSTOMERS; i++) {
            for (int j = i + 1; j < CUSTOMERS; j++) { // 确保i和j是不同的客户地点
                for (int k = 0; k < PATHS_PER_CUSTOMER; k++) {
                    for (int l = 0; l < PATHS_PER_CUSTOMER; l++) {
                        String pathKey1 = "N" + (i + 1) + "P" + (k + 1);
                        String pathKey2 = "N" + (j + 1) + "P" + (l + 1);
                        if (!usedPaths.contains(pathKey1) && !usedPaths.contains(pathKey2) && random.nextBoolean()) {
                            discounts[i][j][k][l] = -random.nextInt(DISCOUNT_MAX);
                            usedPaths.add(pathKey1);
                            usedPaths.add(pathKey2);
                        }
                    }
                }
            }
        }
    }

    // 公共方法访问路径属性
    public int getDistance(int customer, int path) {
        return distances[customer][path];
    }

    public int getCost(int customer, int path) {
        return costs[customer][path];
    }

    public int getTime(int customer, int path) {
        return times[customer][path];
    }

    // 公共方法访问组合优惠
    public int getDiscount(int customer1, int path1, int customer2, int path2) {
        return discounts[customer1][customer2][path1][path2];
    }

    // 其他需要的公共方法...
}
