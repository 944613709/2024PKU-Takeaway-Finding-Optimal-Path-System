package model;

public class Restaurant {
    private int id;
    private double latitude;
    private double longitude;
    
    public Restaurant(int id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
} 