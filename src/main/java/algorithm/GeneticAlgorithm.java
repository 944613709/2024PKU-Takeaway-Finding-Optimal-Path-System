package algorithm;

import model.*;
import java.util.*;

public class GeneticAlgorithm implements OptimizationAlgorithm {
    private final int populationSize;
    private final int maxGenerations;
    private final double mutationRate;
    private final Random random = new Random();
    private Problem problem;
    
    public GeneticAlgorithm(Map<String, Object> parameters) {
        this.populationSize = (int) parameters.getOrDefault("populationSize", 100);
        this.maxGenerations = (int) parameters.getOrDefault("maxGenerations", 1000);
        this.mutationRate = (double) parameters.getOrDefault("mutationRate", 0.1);
    }
    
    @Override
    public Solution solve(Problem problem) {
        this.problem = problem;
        List<Solution> population = initializePopulation();
        
        for (int generation = 0; generation < maxGenerations; generation++) {
            population.sort((a, b) -> Double.compare(calculateFitness(a), calculateFitness(b)));
            
            List<Solution> newPopulation = new ArrayList<>();
            newPopulation.addAll(population.subList(0, populationSize / 10));
            
            while (newPopulation.size() < populationSize) {
                Solution parent1 = selectParent(population);
                Solution parent2 = selectParent(population);
                Solution child = crossover(parent1, parent2);
                if (random.nextDouble() < mutationRate) {
                    mutate(child);
                }
                newPopulation.add(child);
            }
            
            population = newPopulation;
        }
        
        return population.stream()
                .min((a, b) -> Double.compare(calculateFitness(a), calculateFitness(b)))
                .orElse(null);
    }
    
    @Override
    public String getName() {
        return "遗传算法(GA)";
    }
    
    private double calculateFitness(Solution solution) {
        double totalCost = 0;
        double totalTime = 0;
        
        for (int i = 0; i < solution.getPathIndices().length; i++) {
            Path path = problem.getPaths().get("customer_" + i).get(solution.getPathIndices()[i]);
            totalCost += path.getDistance() + path.getCost();
            totalTime += path.getTime();
        }
        
        if (totalTime > problem.getTimeConstraint()) {
            totalCost += (totalTime - problem.getTimeConstraint()) * 1000;
        }
        
        return totalCost;
    }
    
    private List<Solution> initializePopulation() {
        List<Solution> population = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < populationSize; i++) {
            Solution solution = new Solution(problem.getCustomers().size());
            for (int j = 0; j < problem.getCustomers().size(); j++) {
                int pathIndex = random.nextInt(problem.getPaths().get("customer_" + j).size());
                solution.setPathIndex(j, pathIndex);
            }
            updateSolutionMetrics(solution);
            population.add(solution);
        }
        
        return population;
    }
    
    private Solution selectParent(List<Solution> population) {
        Random random = new Random();
        int tournamentSize = 5;
        Solution best = null;
        double bestFitness = Double.MAX_VALUE;
        
        for (int i = 0; i < tournamentSize; i++) {
            Solution candidate = population.get(random.nextInt(population.size()));
            double fitness = calculateFitness(candidate);
            if (best == null || fitness < bestFitness) {
                best = candidate;
                bestFitness = fitness;
            }
        }
        
        return best;
    }
    
    private Solution crossover(Solution parent1, Solution parent2) {
        Random random = new Random();
        Solution child = new Solution(problem.getCustomers().size());
        
        int crossoverPoint = random.nextInt(problem.getCustomers().size());
        for (int i = 0; i < problem.getCustomers().size(); i++) {
            if (i < crossoverPoint) {
                child.setPathIndex(i, parent1.getPathIndices()[i]);
            } else {
                child.setPathIndex(i, parent2.getPathIndices()[i]);
            }
        }
        
        updateSolutionMetrics(child);
        return child;
    }
    
    private void mutate(Solution solution) {
        Random random = new Random();
        int customerIndex = random.nextInt(problem.getCustomers().size());
        int newPathIndex = random.nextInt(problem.getPaths().get("customer_" + customerIndex).size());
        solution.setPathIndex(customerIndex, newPathIndex);
        updateSolutionMetrics(solution);
    }
    
    private void updateSolutionMetrics(Solution solution) {
        double totalCost = 0;
        double totalTime = 0;
        
        for (int i = 0; i < solution.getPathIndices().length; i++) {
            Path path = problem.getPaths().get("customer_" + i).get(solution.getPathIndices()[i]);
            totalCost += path.getDistance() + path.getCost();
            totalTime += path.getTime();
        }
        
        solution.setTotalCost(totalCost);
        solution.setTotalTime(totalTime);
    }
} 