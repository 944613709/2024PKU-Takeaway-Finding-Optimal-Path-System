package model;

public class Solution {
    private int[] pathIndices;  // 每个顾客选择的路径索引
    private double totalCost;   // 总成本
    private double totalTime;   // 总时间
    
    public Solution(int numCustomers) {
        this.pathIndices = new int[numCustomers];
    }
    
    public Solution(int[] pathIndices) {
        this.pathIndices = pathIndices.clone();
    }
    
    // Getters and setters
    public int[] getPathIndices() {
        return pathIndices;
    }
    
    public void setPathIndex(int customerIndex, int pathIndex) {
        pathIndices[customerIndex] = pathIndex;
    }
    
    public double getTotalCost() {
        return totalCost;
    }
    
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    
    public double getTotalTime() {
        return totalTime;
    }
    
    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }
    
    // 克隆方法
    public Solution clone() {
        Solution clone = new Solution(pathIndices);
        clone.setTotalCost(totalCost);
        clone.setTotalTime(totalTime);
        return clone;
    }
} 