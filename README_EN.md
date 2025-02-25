# Food Delivery Path Optimization System

[中文文档](README.md) | [English Documentation](README_EN.md)

## Project Overview
This project implements a food delivery path optimization system that helps optimize delivery routes for multiple restaurants and customers. The system aims to minimize delivery costs while satisfying time constraints.

## Features
- Multiple optimization algorithms support
- Experiment logging and analysis
- Configurable parameters
- Dataset generation and validation
- Comprehensive performance evaluation

## Supported Algorithms
1. **Genetic Algorithm (GA)**
   - Population-based evolutionary algorithm
   - Supports crossover and mutation operations
   - Configurable population size and generation count

2. **Simulated Annealing (SA)**
   - Temperature-based probabilistic optimization
   - Gradual cooling schedule
   - Escapes local optima

3. **Ant Colony Optimization (ACO)**
   - Swarm intelligence based algorithm
   - Pheromone trail optimization
   - Parallel path finding

4. **Tabu Search (TS)**
   - Memory-based search strategy
   - Avoids revisiting recent solutions
   - Efficient local search

5. **Dynamic Programming (DP)**
   - State-based optimization
   - Optimal substructure utilization
   - Memory-efficient implementation

6. **Reinforcement Learning (RL)**
   - Q-learning based approach
   - Experience-driven optimization
   - Adaptive path selection

## Experiment Logging Feature
The system includes a comprehensive experiment logging feature that:
- Records experiment results in Excel format
- Supports multiple experiment types
- Tracks key performance metrics
- Enables data analysis and visualization

### Experiment Types
1. **Effectiveness Experiment**
   - Tests solution quality
   - Compares with optimal solutions
   - Uses three different dataset sizes:
     - Small: 3 restaurants, 5 customers, 3 paths per customer
     - Medium: 5 restaurants, 10 customers, 5 paths per customer
     - Large: 8 restaurants, 15 customers, 8 paths per customer

2. **Efficiency Experiment**
   - Measures computational performance
   - Tests scalability
   - Evaluates time complexity

3. **Stability Experiment**
   - Assesses solution consistency
   - Measures variance in results
   - Tests robustness

4. **Adaptability Experiment**
   - Tests performance under different constraints
   - Evaluates solution flexibility
   - Measures adaptation capability

5. **Algorithm Comparison**
   - Comprehensive algorithm comparison
   - Statistical analysis
   - Performance benchmarking

## Configuration
The system uses a `config.yml` file for configuration:
```yaml
algorithm:
  type: GA  # GA|SA|ACO|TS|DP|RL
  parameters:
    # Algorithm-specific parameters
    ...

dataGeneration:
  strategy: "B"          # Generation strategy: A-Random, B-Feasible
  timeConstraint: 120.0  # Total time constraint
  # Other parameters
  ...
```

## Usage
1. **Basic Usage**
   ```bash
   java -jar delivery-optimizer.jar --algorithm GA
   ```

2. **Custom Configuration**
   ```bash
   java -jar delivery-optimizer.jar --config custom_config.yml
   ```

3. **Experiment Mode**
   ```bash
   java -jar delivery-optimizer.jar --experiment effectiveness
   ```

## Results
The system generates detailed experiment results in the `experiment_results` directory:
- Excel files with experiment data
- Performance metrics
- Comparative analysis
- Statistical measures

## Development
### Prerequisites
- Java 21 or higher
- Maven
- Required dependencies (see pom.xml)

### Building
```bash
mvn clean install
```

### Testing
```bash
mvn test
```

## Contributing
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details. 