package algorithm;

import model.Problem;
import model.Solution;

public interface OptimizationAlgorithm {
    Solution solve(Problem problem);
    String getName();
} 