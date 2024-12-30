import java.util.Random;

public class SimulatedAnnealing {

    private double temperature;
    private double coolingRate;
    private double minTemperature;
    private int[] currentSolution;
    private int[] bestSolution;
    private static TestDataSet testData;

    public SimulatedAnnealing() {
        this.temperature = 1000;
        this.coolingRate = 0.99;
        this.minTemperature = 0.01;
        this.currentSolution = generateRandomSolution();
        this.bestSolution = currentSolution.clone();
    }

    public int[] solve() {
        while (temperature > minTemperature) {
            int[] newSolution = generateNewSolution(currentSolution);
            double currentCost = calculateTotalCost(currentSolution);
            double newCost = calculateTotalCost(newSolution);

            if (newCost < currentCost || Math.random() < Math.exp((currentCost - newCost) / temperature)) {
                currentSolution = newSolution;
                if (newCost < calculateTotalCost(bestSolution)) {
                    bestSolution = newSolution.clone();
                }
            }
            temperature *= coolingRate;
        }
        return bestSolution;
    }

    private int[] generateRandomSolution() {
        int[] solution = new int[TestDataSet.CUSTOMERS];
        for (int i = 0; i < solution.length; i++) {
            solution[i] = new Random().nextInt(TestDataSet.PATHS_PER_CUSTOMER);
        }
        return solution;
    }

    private int[] generateNewSolution(int[] solution) {
        int[] newSolution = solution.clone();
        int i = new Random().nextInt(newSolution.length);
        newSolution[i] = new Random().nextInt(TestDataSet.PATHS_PER_CUSTOMER);
        return newSolution;
    }

    private double calculateTotalCost(int[] solution) {
        double totalCost = 0;
        int totalTime = 0;
        for (int i = 0; i < solution.length; i++) {
            int customer = i;
            int path = solution[i];
            totalTime += testData.getTime(customer, path);
            totalCost += testData.getDistance(customer, path);
            totalCost += testData.getCost(customer, path);
            for (int j = i + 1; j < solution.length; j++) {
                int customer2 = j;
                int path2 = solution[j];
                totalCost += testData.getDiscount(customer, path, customer2, path2);
            }
        }
        totalCost /= (TestDataSet.CUSTOMERS * 2);
        if (totalTime > TestDataSet.TIME_LIMIT) {
            totalCost += TestDataSet.EXTRA_TIME_COST;
        }
        return totalCost;
    }

    private double calculateTotalTime(int[] solution) {
        int totalTime = 0;
        for (int i = 0; i < solution.length; i++) {
            int customer = i;
            int path = solution[i];
            totalTime += testData.getTime(customer, path);
        }
        return totalTime;
    }

    private void printAllDiscounts() {
        for (int i = 0; i < TestDataSet.CUSTOMERS; i++) {
            for (int j = 0; j < TestDataSet.CUSTOMERS; j++) {
                for (int k = 0; k < TestDataSet.PATHS_PER_CUSTOMER; k++) {
                    for (int l = 0; l < TestDataSet.PATHS_PER_CUSTOMER; l++) {
                        if (testData.discounts[i][j][k][l] != 0) {
                            // 打印组合优惠
                            System.out.println("Discount from Customer " + (i + 1) +
                                    " Path " + (k + 1) + " and Customer " + (j + 1) +
                                    " Path " + (l + 1) + ": " + testData.discounts[i][j][k][l]);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        testData = new TestDataSet();
        SimulatedAnnealing sa = new SimulatedAnnealing();
        int[] bestSolution = sa.solve();
        System.out.println("Customer: " + TestDataSet.CUSTOMERS);
        System.out.println("Paths per Customer: " + TestDataSet.PATHS_PER_CUSTOMER);
        System.out.println("Time Limit: " + TestDataSet.TIME_LIMIT);
        System.out.println("Discounts: ");
        sa.printAllDiscounts();
        System.out.println("Best Solution: ");
        for (int i = 0; i < bestSolution.length; i++) {
            System.out.println("Customer: " + (i + 1) + ", Path: " + (bestSolution[i] + 1));
        }
        System.out.println("Total Time: " + sa.calculateTotalTime(bestSolution));
        System.out.println("Total Cost: " + sa.calculateTotalCost(bestSolution));
    }
}
