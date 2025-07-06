package models;

import models.Edge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reservation {
    private final String reservationId;
    private Student student;
    private List<Edge> pathEdges;  // Changed from single Edge to List<Edge>
    private LocalDateTime bookingTime;
    private LocalDateTime travelTime;
    private ReservationStatus status;
    private double totalCost;
    private int seatsReserved;
    private String notes;
    
    public enum ReservationStatus {
        PENDING,     // Waiting for confirmation
        CONFIRMED,   // Confirmed
        CANCELLED,   // Cancelled
        COMPLETED    // Completed
    }
    
    // Legacy constructor for single edge
    public Reservation(Student student, Edge route, LocalDateTime travelTime) {
        this(student, route, travelTime, 1);
    }
    
    // Legacy constructor for single edge with seats
    public Reservation(Student student, Edge route, LocalDateTime travelTime, int seatsReserved) {
        this.reservationId = UUID.randomUUID().toString().substring(0, 8);
        this.student = student;
        this.pathEdges = new ArrayList<>();
        this.pathEdges.add(route);
        this.travelTime = travelTime;
        this.bookingTime = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.totalCost = route.getCost() * seatsReserved;
        this.seatsReserved = seatsReserved;
        this.notes = "";
    }
    
    // New constructor for path reservation
    public Reservation(Student student, List<Edge> pathEdges, LocalDateTime travelTime, int seatsReserved) {
        this.reservationId = UUID.randomUUID().toString().substring(0, 8);
        this.student = student;
        this.pathEdges = new ArrayList<>(pathEdges);
        this.travelTime = travelTime;
        this.bookingTime = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.totalCost = pathEdges.stream().mapToDouble(Edge::getCost).sum() * seatsReserved;
        this.seatsReserved = seatsReserved;
        this.notes = "";
    }
    
    // Getters and Setters
    public String getReservationId() { return reservationId; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    
    public List<Edge> getPathEdges() { return pathEdges; }
    public void setPathEdges(List<Edge> pathEdges) { this.pathEdges = pathEdges; }
    
    // Legacy support - returns first edge of path
    public Edge getRoute() { return pathEdges.isEmpty() ? null : pathEdges.get(0); }
    public void setRoute(Edge route) { 
        this.pathEdges = new ArrayList<>();
        if (route != null) {
            this.pathEdges.add(route);
        }
    }
    
    public LocalDateTime getBookingTime() { return bookingTime; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }
    
    public LocalDateTime getTravelTime() { return travelTime; }
    public void setTravelTime(LocalDateTime travelTime) { this.travelTime = travelTime; }
    
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    
    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    
    public int getSeatsReserved() { return seatsReserved; }
    public void setSeatsReserved(int seatsReserved) { this.seatsReserved = seatsReserved; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Helper methods
    public String getStatusString() {
        switch (status) {
            case PENDING: return "Pending";
            case CONFIRMED: return "Confirmed";
            case CANCELLED: return "Cancelled";
            case COMPLETED: return "Completed";
            default: return "Unknown";
        }
    }
    
    public String getFormattedBookingTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return bookingTime.format(formatter);
    }
    
    public String getFormattedTravelTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return travelTime.format(formatter);
    }
    
    public String getRouteString() {
        if (pathEdges.isEmpty()) return "No route";
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pathEdges.size(); i++) {
            Edge edge = pathEdges.get(i);
            if (i == 0) {
                sb.append(edge.getSource().getName());
            }
            sb.append(" → ").append(edge.getDestination().getName());
        }
        return sb.toString();
    }
    
    public boolean canBeCancelled() {
        return status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED;
    }
    
    public boolean canBeConfirmed() {
        return status == ReservationStatus.PENDING;
    }
    
    @Override
    public String toString() {
        return String.format("Reservation %s: %s - %s (Status: %s, Cost: %.0f T)", 
                           reservationId, student.getName(), getRouteString(), 
                           getStatusString(), totalCost);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reservation reservation = (Reservation) obj;
        return reservationId.equals(reservation.reservationId);
    }
    
    @Override
    public int hashCode() {
        return reservationId.hashCode();
    }
} 