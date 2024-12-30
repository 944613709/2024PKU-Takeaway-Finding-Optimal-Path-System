
import java.util.*;

public class GeneticAlgorithmSolver {

    private static final int POPULATION_SIZE = 50; // 种群规模
    private static final int GENERATIONS = 200; // 最大代数
    private static final double MUTATION_RATE = 0.1; // 变异率
    private static final double CROSSOVER_RATE = 0.8; // 交叉率

    private TestDataSet testDataSet;

    public GeneticAlgorithmSolver(TestDataSet testDataSet) {
        this.testDataSet = testDataSet;
    }

    // 主方法：运行遗传算法
    public int[] solve() {
        List<int[]> population = initializePopulation();
        int[] bestSolution = null;
        double bestFitness = Double.MAX_VALUE;

        for (int generation = 0; generation < GENERATIONS; generation++) {
            List<int[]> newPopulation = new ArrayList<>();

            // 选择和生成新一代
            while (newPopulation.size() < POPULATION_SIZE) {
                int[] parent1 = select(population);
                int[] parent2 = select(population);

                // 交叉
                if (Math.random() < CROSSOVER_RATE) {
                    int[][] offspring = crossover(parent1, parent2);
                    newPopulation.add(offspring[0]);
                    newPopulation.add(offspring[1]);
                } else {
                    newPopulation.add(parent1);
                    newPopulation.add(parent2);
                }
            }

            // 变异
            for (int[] individual : newPopulation) {
                if (Math.random() < MUTATION_RATE) {
                    mutate(individual);
                }
            }

            // 更新种群
            population = newPopulation;

            // 找到当前代最优解
            for (int[] individual : population) {
                double fitness = calculateFitness(individual);
                if (fitness < bestFitness) {
                    bestFitness = fitness;
                    bestSolution = individual.clone();
                }
            }

            // 输出当前代的最优适应度
            System.out.println("Generation " + generation + " - Best Fitness: " + bestFitness);
        }

        return bestSolution;
    }

    // 初始化种群
    private List<int[]> initializePopulation() {
        List<int[]> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            int[] individual = new int[TestDataSet.CUSTOMERS];
            for (int j = 0; j < TestDataSet.CUSTOMERS; j++) {
                individual[j] = (int) (Math.random() * TestDataSet.PATHS_PER_CUSTOMER);
            }
            population.add(individual);
        }
        return population;
    }

    // 适应度函数
    private double calculateFitness(int[] individual) {
        int totalCost = 0;
        int totalTime = 0;

        // 计算路径费用和时间
        for (int i = 0; i < individual.length; i++) {
            int path = individual[i];
            totalCost += testDataSet.getCost(i, path);
            totalTime += testDataSet.getTime(i, path);
        }

        // 超时惩罚
        if (totalTime > TestDataSet.TIME_LIMIT) {
            totalCost += (totalTime - TestDataSet.TIME_LIMIT) * TestDataSet.EXTRA_TIME_COST;
        }

        // 计算路径优惠
        for (int i = 0; i < individual.length; i++) {
            for (int j = i + 1; j < individual.length; j++) {
                int discount = testDataSet.getDiscount(i, individual[i], j, individual[j]);
                totalCost += discount;
            }
        }

        return totalCost; // 适应度值越小越好
    }

    // 选择（锦标赛选择）
    private int[] select(List<int[]> population) {
        int tournamentSize = 5; // 锦标赛规模
        int[] bestIndividual = null;
        double bestFitness = Double.MAX_VALUE;

        for (int i = 0; i < tournamentSize; i++) {
            int[] individual = population.get((int) (Math.random() * population.size()));
            double fitness = calculateFitness(individual);
            if (fitness < bestFitness) {
                bestFitness = fitness;
                bestIndividual = individual;
            }
        }

        return bestIndividual.clone();
    }

    // 交叉操作（单点交叉）
    private int[][] crossover(int[] parent1, int[] parent2) {
        int crossoverPoint = (int) (Math.random() * parent1.length);
        int[] offspring1 = new int[parent1.length];
        int[] offspring2 = new int[parent2.length];

        for (int i = 0; i < parent1.length; i++) {
            if (i < crossoverPoint) {
                offspring1[i] = parent1[i];
                offspring2[i] = parent2[i];
            } else {
                offspring1[i] = parent2[i];
                offspring2[i] = parent1[i];
            }
        }

        return new int[][] { offspring1, offspring2 };
    }

    // 变异操作
    private void mutate(int[] individual) {
        int index = (int) (Math.random() * individual.length);
        individual[index] = (int) (Math.random() * TestDataSet.PATHS_PER_CUSTOMER);
    }

    // 主程序入口
    public static void main(String[] args) {
        TestDataSet testDataSet = new TestDataSet();
        GeneticAlgorithmSolver solver = new GeneticAlgorithmSolver(testDataSet);

        int[] solution = solver.solve();

        // 输出最佳结果
        System.out.println("Best Solution:");
        int totalCost = 0;
        int totalTime = 0;
        for (int i = 0; i < solution.length; i++) {
            int path = solution[i];
            int cost = testDataSet.getCost(i, path);
            int time = testDataSet.getTime(i, path);
            totalCost += cost;
            totalTime += time;
            System.out.println("Customer: " + (i + 1) + ", Path: " + (path + 1) + ", Cost: " + cost + ", Time: " + time);
        }

        // 计算折扣
        for (int i = 0; i < solution.length; i++) {
            for (int j = i + 1; j < solution.length; j++) {
                int discount = testDataSet.getDiscount(i, solution[i], j, solution[j]);
                totalCost += discount;
                if (discount != 0) {
                    System.out.println("Discount from Customer " + (i + 1) + " Path " + (solution[i] + 1) +
                            " and Customer " + (j + 1) + " Path " + (solution[j] + 1) + ": " + discount);
                }
            }
        }

        // 输出总时间和总成本
        if (totalTime > TestDataSet.TIME_LIMIT) {
            totalCost += (totalTime - TestDataSet.TIME_LIMIT) * TestDataSet.EXTRA_TIME_COST;
        }

        System.out.println("Total Time: " + totalTime);
        System.out.println("Total Cost: " + totalCost);
    }
}
