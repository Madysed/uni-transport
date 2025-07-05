public class Edge {
    private Node source;
    private Node destination;
    private double weight; // Distance (kilometers)
    private double cost; // Cost (Toman)
    private int originalCapacity; // Original bus capacity
    private int capacity; // Current bus capacity
    private String operatingHours; // Operating hours
    private double travelTime; // Travel time (hours)
    private int reservedSeats; // Number of reserved seats
    
    public Edge(Node source, Node destination, double weight, double cost, int capacity, String operatingHours) {
        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.cost = cost;
        this.originalCapacity = capacity;
        this.capacity = capacity;
        this.operatingHours = operatingHours;
        this.travelTime = weight / 60.0; // Bus speed: 60 km/h
        this.reservedSeats = 0;
    }
    
    // Getters and Setters
    public Node getSource() { return source; }
    public void setSource(Node source) { this.source = source; }
    
    public Node getDestination() { return destination; }
    public void setDestination(Node destination) { this.destination = destination; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { 
        this.weight = weight;
        this.travelTime = weight / 60.0; // Update travel time
    }
    
    public String getTransportType() { return "Bus"; } // Always bus
    
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public int getOriginalCapacity() { return originalCapacity; }
    public void setOriginalCapacity(int originalCapacity) { 
        this.originalCapacity = originalCapacity;
        this.capacity = originalCapacity; // Reset capacity
    }
    
    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }
    
    public double getTime() { return travelTime; } // For compatibility with old code
    public double getTravelTime() { return travelTime; }
    public void setTime(double time) { this.travelTime = time; }
    
    public int getReservedSeats() { return reservedSeats; }
    public void setReservedSeats(int reservedSeats) { this.reservedSeats = reservedSeats; }
    
    // Capacity management (deprecated - use BookingSystem instead)
    @Deprecated
    public boolean reserveSeat() {
        if (capacity > 0) {
            capacity--;
            reservedSeats++;
            return true;
        }
        return false;
    }
    
    @Deprecated
    public boolean reserveSeats(int seats) {
        if (capacity >= seats) {
            capacity -= seats;
            reservedSeats += seats;
            return true;
        }
        return false;
    }
    
    @Deprecated
    public void releaseSeat() {
        if (reservedSeats > 0) {
            capacity++;
            reservedSeats--;
        }
    }
    
    @Deprecated
    public void releaseSeats(int seats) {
        int seatsToRelease = Math.min(seats, reservedSeats);
        capacity += seatsToRelease;
        reservedSeats -= seatsToRelease;
    }
    
    // Check capacity availability
    public boolean hasCapacity() {
        return capacity > 0;
    }
    
    public boolean hasCapacity(int seats) {
        return capacity >= seats;
    }
    
    public int getAvailableCapacity() {
        return capacity;
    }
    
    public double getCapacityUsagePercent() {
        if (originalCapacity == 0) return 0;
        return (double) reservedSeats / originalCapacity * 100;
    }
    
    // Check if operating at specific time
    public boolean isOperatingAt(String time) {
        // Simplification: assume always active
        // More complex time range logic can be implemented
        return true;
    }
    
    // Reset capacity to initial state
    public void resetCapacity() {
        this.capacity = this.originalCapacity;
        this.reservedSeats = 0;
    }
    
    // Route summary information
    public String getCapacityInfo() {
        return String.format("%d/%d (%d%% full)", 
                           reservedSeats, originalCapacity, 
                           (int) getCapacityUsagePercent());
    }
    
    public String getRouteInfo() {
        return String.format("%s → %s (%.1f km, %.0f T, %s)", 
                           source.getName(), destination.getName(), 
                           weight, cost, getCapacityInfo());
    }
    
    @Override
    public String toString() {
        return source.getName() + " -> " + destination.getName() + 
               " (Distance: " + weight + " km, Cost: " + cost + " T" +
               ", Capacity: " + getCapacityInfo() + ", Hours: " + operatingHours + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Edge edge = (Edge) obj;
        return Double.compare(edge.weight, weight) == 0 &&
               source.equals(edge.source) &&
               destination.equals(edge.destination);
    }
    
    @Override
    public int hashCode() {
        return source.hashCode() + destination.hashCode() + Double.hashCode(weight);
    }
} 