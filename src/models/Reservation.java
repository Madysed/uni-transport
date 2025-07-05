package models;

import models.Edge;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Reservation {
    private final String reservationId;
    private Student student;
    private Edge route;
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
    
    public Reservation(Student student, Edge route, LocalDateTime travelTime) {
        this.reservationId = UUID.randomUUID().toString().substring(0, 8);
        this.student = student;
        this.route = route;
        this.travelTime = travelTime;
        this.bookingTime = LocalDateTime.now();
        this.status = ReservationStatus.PENDING;
        this.totalCost = route.getCost();
        this.seatsReserved = 1;
        this.notes = "";
    }
    
    public Reservation(Student student, Edge route, LocalDateTime travelTime, int seatsReserved) {
        this(student, route, travelTime);
        this.seatsReserved = seatsReserved;
        this.totalCost = route.getCost() * seatsReserved;
    }
    
    // Getters and Setters
    public String getReservationId() { return reservationId; }
    
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    
    public Edge getRoute() { return route; }
    public void setRoute(Edge route) { this.route = route; }
    
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
        return route.getSource().getName() + " → " + route.getDestination().getName();
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