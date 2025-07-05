import java.util.*;

public class SmartDijkstra {
    
    public static class PathResult {
        private List<Node> path;
        private double totalCost;
        private double totalTime;
        private double totalDistance;
        private boolean hasCapacity;
        private List<Edge> usedEdges;
        private String failureReason;
        private List<Node> animationSequence;
        
        public PathResult(List<Node> path, double totalCost, double totalTime, double totalDistance, 
                         boolean hasCapacity, List<Edge> usedEdges) {
            this.path = path;
            this.totalCost = totalCost;
            this.totalTime = totalTime;
            this.totalDistance = totalDistance;
            this.hasCapacity = hasCapacity;
            this.usedEdges = usedEdges;
            this.animationSequence = new ArrayList<>();
            this.failureReason = "";
        }
        
        public PathResult(String failureReason) {
            this.failureReason = failureReason;
            this.path = new ArrayList<>();
            this.usedEdges = new ArrayList<>();
            this.animationSequence = new ArrayList<>();
            this.hasCapacity = false;
        }
        
        // Getters
        public List<Node> getPath() { return path; }
        public double getTotalCost() { return totalCost; }
        public double getTotalTime() { return totalTime; }
        public double getTotalDistance() { return totalDistance; }
        public boolean hasCapacity() { return hasCapacity; }
        public List<Edge> getUsedEdges() { return usedEdges; }
        public String getFailureReason() { return failureReason; }
        public List<Node> getAnimationSequence() { return animationSequence; }
        public void setAnimationSequence(List<Node> sequence) { this.animationSequence = sequence; }
        
        public boolean isSuccessful() {
            return !path.isEmpty() && hasCapacity;
        }
        
        public String getPathSummary() {
            if (!isSuccessful()) {
                return "No valid path found: " + failureReason;
            }
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getName());
                if (i < path.size() - 1) {
                    sb.append(" → ");
                }
            }
            return String.format("%s (Cost: %.0f T, Time: %.1f h, Distance: %.1f km)", 
                               sb.toString(), totalCost, totalTime, totalDistance);
        }
    }
    
    // Different weights for combining criteria
    public static class OptimizationWeights {
        public double costWeight = 0.4;     // 40% importance of cost
        public double timeWeight = 0.4;     // 40% importance of time
        public double distanceWeight = 0.2; // 20% importance of distance
        public double capacityPenalty = 1000.0; // Penalty for no capacity
        
        public OptimizationWeights() {}
        
        public OptimizationWeights(double costWeight, double timeWeight, double distanceWeight) {
            this.costWeight = costWeight;
            this.timeWeight = timeWeight;
            this.distanceWeight = distanceWeight;
        }
    }
    
    public static PathResult findOptimalPath(List<Node> nodes, Node start, Node destination, 
                                           Student student, OptimizationWeights weights) {
        return findOptimalPath(nodes, start, destination, student, weights, null);
    }
    
    public static PathResult findOptimalPath(List<Node> nodes, Node start, Node destination, 
                                           Student student, OptimizationWeights weights, 
                                           List<Node> animationCallback) {
        
        if (start.equals(destination)) {
            return new PathResult("Start and destination are the same");
        }
        
        // Reset all nodes
        for (Node node : nodes) {
            node.setDistance(Double.MAX_VALUE);
            node.setParent(null);
            node.setVisited(false);
        }
        
        start.setDistance(0);
        
        // Prioritize based on combined factor
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
            Comparator.comparingDouble(Node::getDistance)
        );
        
        priorityQueue.offer(start);
        BookingSystem bookingSystem = BookingSystem.getInstance();
        
        // Store visit order for animation
        List<Node> visitSequence = new ArrayList<>();
        
        // Maps for storing path information
        Map<Node, Double> costToNode = new HashMap<>();
        Map<Node, Double> timeToNode = new HashMap<>();
        Map<Node, Edge> edgeToNode = new HashMap<>();
        
        costToNode.put(start, 0.0);
        timeToNode.put(start, 0.0);
        
        while (!priorityQueue.isEmpty()) {
            Node current = priorityQueue.poll();
            
            if (current.isVisited()) continue;
            
            current.setVisited(true);
            visitSequence.add(current);
            
            // Callback for animation
            if (animationCallback != null) {
                animationCallback.add(current);
            }
            
            // If we reached the destination
            if (current.equals(destination)) {
                break;
            }
            
            // Check all adjacent edges
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getDestination();
                
                if (neighbor.isVisited()) continue;
                
                // Check capacity
                boolean hasCapacityForRoute = bookingSystem.hasCapacity(edge, 1);
                
                // Calculate different costs
                double edgeCost = edge.getCost();
                double edgeTime = edge.getTravelTime();
                double edgeDistance = edge.getWeight();
                
                // Check student budget
                double totalCostSoFar = costToNode.getOrDefault(current, 0.0) + edgeCost;
                if (totalCostSoFar > student.getMaxBudget()) {
                    continue; // Skip routes that exceed budget
                }
                
                // Calculate combined factor
                double combinedWeight = calculateCombinedWeight(
                    edgeCost, edgeTime, edgeDistance, hasCapacityForRoute, weights
                );
                
                double newDistance = current.getDistance() + combinedWeight;
                
                if (newDistance < neighbor.getDistance()) {
                    neighbor.setDistance(newDistance);
                    neighbor.setParent(current);
                    
                    // Store path information
                    costToNode.put(neighbor, totalCostSoFar);
                    timeToNode.put(neighbor, timeToNode.getOrDefault(current, 0.0) + edgeTime);
                    edgeToNode.put(neighbor, edge);
                    
                    priorityQueue.offer(neighbor);
                }
            }
        }
        
        // Build final path
        if (!destination.isVisited() || destination.getParent() == null) {
            return new PathResult("No path found to destination");
        }
        
        return buildPath(destination, costToNode, timeToNode, edgeToNode, visitSequence);
    }
    
    private static double calculateCombinedWeight(double cost, double time, double distance, 
                                                boolean hasCapacity, OptimizationWeights weights) {
        
        // Normalize values (assumption: max cost 200000, max time 20 hours, max distance 2000 km)
        double normalizedCost = cost / 200000.0;
        double normalizedTime = time / 20.0;
        double normalizedDistance = distance / 2000.0;
        
        double combinedWeight = (normalizedCost * weights.costWeight) + 
                               (normalizedTime * weights.timeWeight) + 
                               (normalizedDistance * weights.distanceWeight);
        
        // Add penalty if no capacity
        if (!hasCapacity) {
            combinedWeight += weights.capacityPenalty;
        }
        
        return combinedWeight;
    }
    
    private static PathResult buildPath(Node destination, Map<Node, Double> costToNode, 
                                      Map<Node, Double> timeToNode, Map<Node, Edge> edgeToNode,
                                      List<Node> visitSequence) {
        
        List<Node> path = new ArrayList<>();
        List<Edge> usedEdges = new ArrayList<>();
        Node current = destination;
        boolean hasCapacity = true;
        
        // Build path from destination to source
        while (current != null) {
            path.add(0, current);
            if (current.getParent() != null) {
                Edge edge = edgeToNode.get(current);
                if (edge != null) {
                    usedEdges.add(0, edge);
                    // Check capacity of all edges
                    if (!BookingSystem.getInstance().hasCapacity(edge, 1)) {
                        hasCapacity = false;
                    }
                }
            }
            current = current.getParent();
        }
        
        double totalCost = costToNode.getOrDefault(destination, 0.0);
        double totalTime = timeToNode.getOrDefault(destination, 0.0);
        double totalDistance = usedEdges.stream().mapToDouble(Edge::getWeight).sum();
        
        PathResult result = new PathResult(path, totalCost, totalTime, totalDistance, hasCapacity, usedEdges);
        result.setAnimationSequence(visitSequence);
        
        return result;
    }
    
    // Generate alternative paths
    public static List<PathResult> findAlternativePaths(List<Node> nodes, Node start, Node destination, 
                                                       Student student, int maxAlternatives) {
        
        List<PathResult> alternatives = new ArrayList<>();
        
        // Cost-optimized path
        OptimizationWeights costOptimized = new OptimizationWeights(0.8, 0.1, 0.1);
        PathResult costPath = findOptimalPath(nodes, start, destination, student, costOptimized);
        if (costPath.isSuccessful()) alternatives.add(costPath);
        
        // Time-optimized path
        OptimizationWeights timeOptimized = new OptimizationWeights(0.1, 0.8, 0.1);
        PathResult timePath = findOptimalPath(nodes, start, destination, student, timeOptimized);
        if (timePath.isSuccessful()) alternatives.add(timePath);
        
        // Distance-optimized path
        OptimizationWeights distanceOptimized = new OptimizationWeights(0.1, 0.1, 0.8);
        PathResult distancePath = findOptimalPath(nodes, start, destination, student, distanceOptimized);
        if (distancePath.isSuccessful()) alternatives.add(distancePath);
        
        // Balanced path
        OptimizationWeights balanced = new OptimizationWeights(0.33, 0.33, 0.34);
        PathResult balancedPath = findOptimalPath(nodes, start, destination, student, balanced);
        if (balancedPath.isSuccessful()) alternatives.add(balancedPath);
        
        // Remove duplicate paths and sort
        alternatives = removeDuplicatePaths(alternatives);
        alternatives.sort(Comparator.comparingDouble(PathResult::getTotalCost));
        
        return alternatives.subList(0, Math.min(alternatives.size(), maxAlternatives));
    }
    
    private static List<PathResult> removeDuplicatePaths(List<PathResult> paths) {
        Set<String> seenPaths = new HashSet<>();
        List<PathResult> uniquePaths = new ArrayList<>();
        
        for (PathResult path : paths) {
            String pathSignature = path.getPath().stream()
                .map(Node::getName)
                .reduce("", (a, b) -> a + "->" + b);
            
            if (!seenPaths.contains(pathSignature)) {
                seenPaths.add(pathSignature);
                uniquePaths.add(path);
            }
        }
        
        return uniquePaths;
    }
    
    // Helper function for displaying path details
    public static String getDetailedPathInfo(PathResult pathResult) {
        if (!pathResult.isSuccessful()) {
            return "Path calculation failed: " + pathResult.getFailureReason();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("=== Path Details ===\n");
        sb.append("Route: ").append(pathResult.getPathSummary()).append("\n");
        sb.append("Capacity Available: ").append(pathResult.hasCapacity() ? "Yes" : "No").append("\n");
        
        if (!pathResult.getUsedEdges().isEmpty()) {
            sb.append("\n=== Route Segments ===\n");
            for (int i = 0; i < pathResult.getUsedEdges().size(); i++) {
                Edge edge = pathResult.getUsedEdges().get(i);
                BookingSystem bs = BookingSystem.getInstance();
                sb.append(String.format("%d. %s → %s\n", i + 1, 
                         edge.getSource().getName(), edge.getDestination().getName()));
                sb.append(String.format("   Distance: %.1f km, Cost: %.0f T, Time: %.1f h\n", 
                         edge.getWeight(), edge.getCost(), edge.getTravelTime()));
                sb.append(String.format("   Available Seats: %d/%d\n", 
                         bs.getAvailableCapacity(edge), edge.getOriginalCapacity()));
            }
        }
        
        return sb.toString();
    }
} 