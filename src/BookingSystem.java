import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BookingSystem {
    private List<Reservation> reservations;
    private List<Student> students;
    private Map<String, Integer> routeCapacityUsage; // Key: route_id, Value: usage count
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
    
    // Reservation management
    public ReservationResult makeReservation(Student student, Edge route, LocalDateTime travelTime) {
        return makeReservation(student, route, travelTime, 1);
    }
    
    public ReservationResult makeReservation(Student student, Edge route, LocalDateTime travelTime, int seatsRequested) {
        // Check capacity
        if (!hasCapacity(route, seatsRequested)) {
            return new ReservationResult(false, "Not enough capacity on this route.", null, findAlternativeRoutes(route));
        }
        
        // Check student budget
        double totalCost = route.getCost() * seatsRequested;
        if (totalCost > student.getMaxBudget()) {
            return new ReservationResult(false, "Not enough budget for this route.", null, findCheaperRoutes(route, student.getMaxBudget()));
        }
        
        // Create reservation
        Reservation reservation = new Reservation(student, route, travelTime, seatsRequested);
        reservations.add(reservation);
        
        // Update capacity usage
        String routeKey = getRouteKey(route);
        routeCapacityUsage.put(routeKey, routeCapacityUsage.getOrDefault(routeKey, 0) + seatsRequested);
        
        return new ReservationResult(true, "Reservation made successfully.", reservation, null);
    }
    
    public boolean cancelReservation(String reservationId) {
        Optional<Reservation> reservationOpt = reservations.stream()
            .filter(r -> r.getReservationId().equals(reservationId))
            .findFirst();
        
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            if (reservation.canBeCancelled()) {
                reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
                
                // Free up capacity
                String routeKey = getRouteKey(reservation.getRoute());
                int currentUsage = routeCapacityUsage.getOrDefault(routeKey, 0);
                routeCapacityUsage.put(routeKey, Math.max(0, currentUsage - reservation.getSeatsReserved()));
                
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
            .filter(r -> getRouteKey(r.getRoute()).equals(routeKey))
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
    public static class ReservationResult {
        private boolean success;
        private String message;
        private Reservation reservation;
        private List<Edge> alternativeRoutes;
        
        public ReservationResult(boolean success, String message, Reservation reservation, List<Edge> alternativeRoutes) {
            this.success = success;
            this.message = message;
            this.reservation = reservation;
            this.alternativeRoutes = alternativeRoutes != null ? alternativeRoutes : new ArrayList<>();
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Reservation getReservation() { return reservation; }
        public List<Edge> getAlternativeRoutes() { return alternativeRoutes; }
    }
} 