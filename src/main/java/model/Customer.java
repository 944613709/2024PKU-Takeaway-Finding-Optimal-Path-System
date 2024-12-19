package model;

public class Customer {
    private int id;
    private double latitude;
    private double longitude;
    
    public Customer(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
} 