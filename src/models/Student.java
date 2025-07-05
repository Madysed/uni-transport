package models;

import java.time.LocalDateTime;
import java.util.UUID;

public class Student {
    private String id;
    private String name;
    private String phoneNumber;
    private String email;
    private Node currentLocation;
    private Node destination;
    private LocalDateTime travelTime;
    private boolean isActive;
    private String preferredTransport;
    private double maxBudget;
    
    public Student(String name, String phoneNumber, String email) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.isActive = true;
        this.preferredTransport = "Bus";
        this.maxBudget = 100000.0; // Default 100,000 Toman
    }
    
    public Student(String name, String phoneNumber, String email, Node currentLocation, Node destination) {
        this(name, phoneNumber, email);
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.travelTime = LocalDateTime.now().plusHours(1); // Travel one hour later
    }
    
    // Getters and Setters
    public String getId() { return id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Node getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Node currentLocation) { this.currentLocation = currentLocation; }
    
    public Node getDestination() { return destination; }
    public void setDestination(Node destination) { this.destination = destination; }
    
    public LocalDateTime getTravelTime() { return travelTime; }
    public void setTravelTime(LocalDateTime travelTime) { this.travelTime = travelTime; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    public String getPreferredTransport() { return preferredTransport; }
    public void setPreferredTransport(String preferredTransport) { this.preferredTransport = preferredTransport; }
    
    public double getMaxBudget() { return maxBudget; }
    public void setMaxBudget(double maxBudget) { this.maxBudget = maxBudget; }
    
    @Override
    public String toString() {
        return name + " (" + id + ") - " + 
               (currentLocation != null ? currentLocation.getName() : "No location") + 
               " → " + 
               (destination != null ? destination.getName() : "No destination");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Student student = (Student) obj;
        return id.equals(student.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
} 