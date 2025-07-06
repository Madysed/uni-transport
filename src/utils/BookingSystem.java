package utils;

import models.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BookingSystem {
    private final List<Reservation> reservations;
    private final List<Student> students;
    private final Map<String, Integer> routeCapacityUsage; // Key: route_id, Value: usage count
    private static BookingSystem instance;
    
    private BookingSystem() {
        this.reservations = new ArrayList<>();
        this.students = new ArrayList<>();
        this.routeCapacityUsage = new HashMap<>();
    }
    
    public static BookingSystem getInstance() {
        if (instance == null) {
            instance = new BookingSystem();
        }
        return instance;
    }
    
    // Student management
    public void addStudent(Student student) {
        if (!students.contains(student)) {
            students.add(student);
        }
    }
    
    public void removeStudent(Student student) {
        students.remove(student);
        // Cancel reservations for this student
        reservations.stream()
            .filter(r -> r.getStudent().equals(student))
            .forEach(r -> r.setStatus(Reservation.ReservationStatus.CANCELLED));
    }
    
    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }
    
    public Student findStudentById(String id) {
        return students.stream()
            .filter(s -> s.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    // Path Reservation management
    public ReservationResult makePathReservation(Student student, List<Edge> pathEdges, LocalDateTime travelTime) {
        return makePathReservation(student, pathEdges, travelTime, 1);
    }
    
    public ReservationResult makePathReservation(Student student, List<Edge> pathEdges, LocalDateTime travelTime, int seatsRequested) {
        if (pathEdges == null || pathEdges.isEmpty()) {
            return new ReservationResult(false, "Invalid path: no edges provided.", null, null);
        }
        
        // Check capacity for all edges in the path
        for (Edge edge : pathEdges) {
            if (!hasCapacity(edge, seatsRequested)) {
                return new ReservationResult(false, 
                    "Not enough capacity on route: " + edge.getSource().getName() + " → " + edge.getDestination().getName(), 
                    null, 
                    findAlternativeRoutes(edge));
            }
        }
        
        // Calculate total cost for the path
        double totalCost = pathEdges.stream().mapToDouble(Edge::getCost).sum() * seatsRequested;
        
        // Check student budget
        if (totalCost > student.getMaxBudget()) {
            return new ReservationResult(false, "Not enough budget for this path.", null, findCheaperRoutes(pathEdges.get(0), student.getMaxBudget()));
        }
        
        // Create reservation for the complete path
        Reservation reservation = new Reservation(student, pathEdges, travelTime, seatsRequested);
        reservations.add(reservation);
        
        // Update capacity usage for all edges in the path
        for (Edge edge : pathEdges) {
            String routeKey = getRouteKey(edge);
            routeCapacityUsage.put(routeKey, routeCapacityUsage.getOrDefault(routeKey, 0) + seatsRequested);
        }
        
        return new ReservationResult(true, "Path reservation made successfully.", reservation, null);
    }
    
    // Single edge reservation (legacy support)
    public ReservationResult makeReservation(Student student, Edge route, LocalDateTime travelTime) {
        return makeReservation(student, route, travelTime, 1);
    }
    
    public ReservationResult makeReservation(Student student, Edge route, LocalDateTime travelTime, int seatsRequested) {
        return makePathReservation(student, Collections.singletonList(route), travelTime, seatsRequested);
    }
    
    public boolean cancelReservation(String reservationId) {
        Optional<Reservation> reservationOpt = reservations.stream()
            .filter(r -> r.getReservationId().equals(reservationId))
            .findFirst();
        
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (reservation.canBeCancelled()) {
                reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                
                // Free up capacity for all edges in the path
                for (Edge edge : reservation.getPathEdges()) {
                    String routeKey = getRouteKey(edge);
                    int currentUsage = routeCapacityUsage.getOrDefault(routeKey, 0);
                    routeCapacityUsage.put(routeKey, Math.max(0, currentUsage - reservation.getSeatsReserved()));
                }
                
                return true;
            }
        }
        return false;
    }
    
    public boolean confirmReservation(String reservationId) {
        Optional<Reservation> reservationOpt = reservations.stream()
            .filter(r -> r.getReservationId().equals(reservationId))
            .findFirst();
        
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (reservation.canBeConfirmed()) {
                reservation.setStatus(Reservation.ReservationStatus.CONFIRMED);
                return true;
            }
        }
        return false;
    }
    
    // Capacity check
    public boolean hasCapacity(Edge route, int seatsRequested) {
        String routeKey = getRouteKey(route);
        int currentUsage = routeCapacityUsage.getOrDefault(routeKey, 0);
        return (currentUsage + seatsRequested) <= route.getCapacity();
    }
    
    public int getAvailableCapacity(Edge route) {
        String routeKey = getRouteKey(route);
        int currentUsage = routeCapacityUsage.getOrDefault(routeKey, 0);
        return Math.max(0, route.getCapacity() - currentUsage);
    }
    
    public double getCapacityUsagePercent(Edge route) {
        String routeKey = getRouteKey(route);
        int currentUsage = routeCapacityUsage.getOrDefault(routeKey, 0);
        return route.getCapacity() > 0 ? (double) currentUsage / route.getCapacity() * 100 : 0;
    }
    
    // Find alternative routes
    public List<Edge> findAlternativeRoutes(Edge originalRoute) {
        // Find routes starting from same source
        List<Edge> alternatives = new ArrayList<>();
        
        Node source = originalRoute.getSource();
        for (Edge edge : source.getEdges()) {
            if (!edge.equals(originalRoute) && hasCapacity(edge, 1)) {
                alternatives.add(edge);
            }
        }
        
        // Sort by cost
        alternatives.sort(Comparator.comparingDouble(Edge::getCost));
        
        return alternatives;
    }
    
    public List<Edge> findCheaperRoutes(Edge originalRoute, double maxBudget) {
        List<Edge> cheaper = new ArrayList<>();
        
        Node source = originalRoute.getSource();
        for (Edge edge : source.getEdges()) {
            if (edge.getCost() <= maxBudget && hasCapacity(edge, 1)) {
                cheaper.add(edge);
            }
        }
        
        cheaper.sort(Comparator.comparingDouble(Edge::getCost));
        
        return cheaper;
    }
    
    // Reporting
    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }
    
    public List<Reservation> getReservationsForStudent(Student student) {
        return reservations.stream()
            .filter(r -> r.getStudent().equals(student))
            .collect(Collectors.toList());
    }
    
    public List<Reservation> getReservationsForRoute(Edge route) {
        String routeKey = getRouteKey(route);
        return reservations.stream()
            .filter(r -> r.getPathEdges().stream().anyMatch(e -> getRouteKey(e).equals(routeKey)))
            .collect(Collectors.toList());
    }
    
    public List<Reservation> getActiveReservations() {
        return reservations.stream()
            .filter(r -> r.getStatus() == Reservation.ReservationStatus.PENDING || 
                        r.getStatus() == Reservation.ReservationStatus.CONFIRMED)
            .collect(Collectors.toList());
    }
    
    public Map<String, Integer> getRouteUsageStatistics() {
        return new HashMap<>(routeCapacityUsage);
    }
    
    // Helper methods
    private String getRouteKey(Edge route) {
        return route.getSource().getName() + "->" + route.getDestination().getName();
    }

    // Reservation result class
        public record ReservationResult(boolean success, String message, Reservation reservation,
                                        List<Edge> alternativeRoutes) {
            public ReservationResult(boolean success, String message, Reservation reservation, List<Edge> alternativeRoutes) {
                this.success = success;
                this.message = message;
                this.reservation = reservation;
                this.alternativeRoutes = alternativeRoutes != null ? alternativeRoutes : new ArrayList<>();
            }
        }
} 