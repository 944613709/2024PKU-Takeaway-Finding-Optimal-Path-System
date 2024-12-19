package model;

public class Path {
    private double distance;
    private double cost;
    private double time;
    
    public Path(double distance, double cost, double time) {
        this.distance = distance;
        this.cost = cost;
        this.time = time;
    }
    
    // Getters
    public double getDistance() { return distance; }
    public double getCost() { return cost; }
    public double getTime() { return time; }
} 