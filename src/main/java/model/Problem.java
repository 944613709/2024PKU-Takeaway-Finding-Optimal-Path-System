package model;

import java.util.*;

public class Problem {
    private List<Customer> customers;
    private Map<String, List<Path>> paths;
    private double timeConstraint;
    
    public Problem(List<Customer> customers, Map<String, List<Path>> paths, double timeConstraint) {
        this.customers = customers;
        this.paths = paths;
        this.timeConstraint = timeConstraint;
    }
    
    // Getters
    public List<Customer> getCustomers() { return customers; }
    public Map<String, List<Path>> getPaths() { return paths; }
    public double getTimeConstraint() { return timeConstraint; }
} 