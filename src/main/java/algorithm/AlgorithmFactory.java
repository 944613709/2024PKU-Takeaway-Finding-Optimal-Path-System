package algorithm;

import java.util.Map;

public class AlgorithmFactory {
    public static OptimizationAlgorithm createAlgorithm(String type, Map<String, Object> parameters) {
        return switch (type.toUpperCase()) {
            case "GA" -> new GeneticAlgorithm(parameters);
            case "SA" -> new SimulatedAnnealing(parameters);
            case "ACO" -> new AntColony(parameters);
            case "TS" -> new TabuSearch(parameters);
            default -> throw new IllegalArgumentException("Unknown algorithm type: " + type);
        };
    }
} 