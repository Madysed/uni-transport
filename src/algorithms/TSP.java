package algorithms;

import models.Edge;
import models.Node;

import java.util.*;

public class TSP {
    
    public enum OptimizationType {
        COST,
        TIME,
        DISTANCE
    }
    
    public static class TSPResult {
        private final List<Node> path;
        private final double totalCost;
        private final double totalTime;
        private List<Node> visitedOrder;
        private String details;
        
        public TSPResult(List<Node> path, double totalCost, double totalTime) {
            this.path = path;
            this.totalCost = totalCost;
            this.totalTime = totalTime;
            this.visitedOrder = new ArrayList<>();
            this.details = "";
        }
        
        public List<Node> getPath() { return path; }
        public double getTotalCost() { return totalCost; }
        public double getTotalTime() { return totalTime; }
        public List<Node> getVisitedOrder() { return visitedOrder; }
        public void setVisitedOrder(List<Node> visitedOrder) { this.visitedOrder = visitedOrder; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        
        public String getRouteDescription() {
            if (path.isEmpty()) return "No route found";
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getName());
                if (i < path.size() - 1) {
                    sb.append(" → ");
                }
            }
            return sb.toString();
        }
    }
    
    // Cost and time matrices
    private static double[][] costMatrix;
    private static double[][] timeMatrix;
    private static List<Node> nodeList;
    
    // TSP with Bitmasking and Dynamic Programming (simple and efficient)
    public static TSPResult solveTSPWithBitmasking(List<Node> selectedUniversities, Node startUniversity, OptimizationType optimizationType) {
        if (selectedUniversities.size() <= 1) {
            return new TSPResult(selectedUniversities, 0, 0);
        }
        
        // Convert to list for indexing
        List<Node> nodeList = new ArrayList<>(selectedUniversities);
        int startIndex = nodeList.indexOf(startUniversity);
        if (startIndex == -1) {
            startIndex = 0;
            startUniversity = nodeList.get(0);
        }
        
        int n = nodeList.size();
        double[][] costMatrix = new double[n][n];
        double[][] timeMatrix = new double[n][n];
        
        // Initialize matrices
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                costMatrix[i][j] = Double.MAX_VALUE;
                timeMatrix[i][j] = Double.MAX_VALUE;
            }
            costMatrix[i][i] = 0;
            timeMatrix[i][i] = 0;
        }
        
        // Fill matrices with direct edges only
        for (int i = 0; i < n; i++) {
            Node from = nodeList.get(i);
            for (Edge edge : from.getEdges()) {
                int j = nodeList.indexOf(edge.getDestination());
                if (j != -1) {
                    costMatrix[i][j] = edge.getCost();
                    timeMatrix[i][j] = edge.getTravelTime();
                }
            }
        }
        
        // DP state: dp[mask][last] = minimum cost ending at 'last' visiting all in mask
        double[][] dp = new double[1 << n][n];
        int[][] parent = new int[1 << n][n];
        
        // Initialize
        for (int i = 0; i < (1 << n); i++) {
            Arrays.fill(dp[i], Double.MAX_VALUE);
            Arrays.fill(parent[i], -1);
        }
        
        // Start from first university
        dp[1 << startIndex][startIndex] = 0;
        
        // Fill DP table
        for (int mask = 0; mask < (1 << n); mask++) {
            for (int last = 0; last < n; last++) {
                if ((mask & (1 << last)) == 0 || dp[mask][last] == Double.MAX_VALUE) continue;
                
                for (int next = 0; next < n; next++) {
                    if ((mask & (1 << next)) != 0) continue;
                    
                    double cost = optimizationType == OptimizationType.TIME ? 
                                timeMatrix[last][next] : costMatrix[last][next];
                    
                    if (cost != Double.MAX_VALUE) {
                        int nextMask = mask | (1 << next);
                        double newCost = dp[mask][last] + cost;
                        
                        if (newCost < dp[nextMask][next]) {
                            dp[nextMask][next] = newCost;
                            parent[nextMask][next] = last;
                        }
                    }
                }
            }
        }
        
        // Find best tour that returns to start
        double minTotalCost = Double.MAX_VALUE;
        int bestLast = -1;
        int allVisited = (1 << n) - 1;
        
        for (int last = 0; last < n; last++) {
            if (last == startIndex) continue;
            
            double returnCost = optimizationType == OptimizationType.TIME ? 
                              timeMatrix[last][startIndex] : costMatrix[last][startIndex];
            
            if (returnCost != Double.MAX_VALUE && dp[allVisited][last] != Double.MAX_VALUE) {
                double totalCost = dp[allVisited][last] + returnCost;
                if (totalCost < minTotalCost) {
                    minTotalCost = totalCost;
                    bestLast = last;
                }
            }
        }
        
        // Reconstruct path
        List<Node> path = new ArrayList<>();
        double totalCost = 0;
        double totalTime = 0;
        
        if (bestLast != -1) {
            // Get the sequence of cities
            List<Integer> sequence = new ArrayList<>();
            int currentMask = allVisited;
            int currentLast = bestLast;
            
            while (currentLast != -1) {
                sequence.add(currentLast);
                int nextLast = parent[currentMask][currentLast];
                currentMask ^= (1 << currentLast);
                currentLast = nextLast;
            }
            Collections.reverse(sequence);
            sequence.add(startIndex); // Return to start
            
            // Build path and calculate costs
            for (int i = 0; i < sequence.size(); i++) {
                Node current = nodeList.get(sequence.get(i));
                path.add(current);
                
                if (i < sequence.size() - 1) {
                    Node next = nodeList.get(sequence.get(i + 1));
                    Edge edge = findDirectEdge(current, next);
                    if (edge != null) {
                        totalCost += edge.getCost();
                        totalTime += edge.getTravelTime();
                    }
                }
            }
        }
        
        // Create result
        TSPResult result = new TSPResult(path, totalCost, totalTime);
        result.setVisitedOrder(new ArrayList<>(path));
        
        // Create details
        StringBuilder details = new StringBuilder();
        details.append("Smart TSP Solution Details:\n");
        details.append(String.format("Universities visited: %d\n", selectedUniversities.size()));
        details.append(String.format("Total path length: %d nodes\n", path.size()));
        details.append(String.format("Total cost: %.0f T\n", totalCost));
        details.append(String.format("Total time: %.1f hours\n", totalTime));
        details.append("\nPath: ");
        for (Node node : path) {
            details.append(node.getName()).append(" -> ");
        }
        details.setLength(details.length() - 4); // Remove last arrow
        result.setDetails(details.toString());
        
        return result;
    }
    
    // Helper method to reconstruct path between two nodes
    private static List<Node> reconstructPath(List<Node> nodeList, int from, int to, int[][] nextNode) {
        List<Node> path = new ArrayList<>();
        path.add(nodeList.get(from));
        
        while (from != to) {
            from = nextNode[from][to];
            if (from == -1) break;
            path.add(nodeList.get(from));
        }
        
        return path;
    }
    
    // Create cost and time matrices
    private static void createMatrices(List<Node> nodes) {
        int n = nodes.size();
        costMatrix = new double[n][n];
        timeMatrix = new double[n][n];
        
        // Initialize matrices with infinity
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    costMatrix[i][j] = 0;
                    timeMatrix[i][j] = 0;
                } else {
                    costMatrix[i][j] = Double.MAX_VALUE;
                    timeMatrix[i][j] = Double.MAX_VALUE;
                }
            }
        }
        
        // Fill matrices only with existing edges
        for (int i = 0; i < n; i++) {
            Node from = nodes.get(i);
            for (Edge edge : from.getEdges()) {
                int j = nodes.indexOf(edge.getDestination());
                if (j != -1) {
                    costMatrix[i][j] = edge.getCost();
                    timeMatrix[i][j] = edge.getTravelTime();
                }
            }
        }
        
        // Run Floyd-Warshall to find shortest paths between all pairs
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (costMatrix[i][k] != Double.MAX_VALUE && costMatrix[k][j] != Double.MAX_VALUE) {
                        double newCost = costMatrix[i][k] + costMatrix[k][j];
                        if (newCost < costMatrix[i][j]) {
                            costMatrix[i][j] = newCost;
                            timeMatrix[i][j] = timeMatrix[i][k] + timeMatrix[k][j];
                        }
                    }
                }
            }
        }
    }
    
    // Find direct edge between two nodes
    private static Edge findDirectEdge(Node from, Node to) {
        for (Edge edge : from.getEdges()) {
            if (edge.getDestination().equals(to)) {
                return edge;
            }
        }
        return null;
    }
    
    // Calculate Euclidean distance
    private static double calculateDistance(Node a, Node b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // Simple TSP with Nearest Neighbor (for comparison)
    public static TSPResult solveTSPSimple(List<Node> universities, Node startUniversity) {
        if (universities.size() <= 1) {
            return new TSPResult(universities, 0, 0);
        }
        
        List<Node> path = new ArrayList<>();
        Set<Node> visited = new HashSet<>();
        Node current = startUniversity;
        double totalCost = 0;
        double totalTime = 0;
        
        path.add(current);
        visited.add(current);
        
        // Continue until all universities are visited
        while (visited.size() < universities.size()) {
            Node nearest = null;
            double minCost = Double.MAX_VALUE;
            
            // Find nearest university
            for (Node uni : universities) {
                if (!visited.contains(uni)) {
                    Edge edge = findDirectEdge(current, uni);
                    double cost;
                    if (edge != null) {
                        cost = edge.getCost();
                    } else {
                        double distance = calculateDistance(current, uni);
                        cost = distance * 150;
                    }
                    
                    if (cost < minCost) {
                        minCost = cost;
                        nearest = uni;
                    }
                }
            }
            
            if (nearest != null) {
                path.add(nearest);
                visited.add(nearest);
                totalCost += minCost;
                
                Edge edge = findDirectEdge(current, nearest);
                if (edge != null) {
                    totalTime += edge.getTravelTime();
                } else {
                    totalTime += calculateDistance(current, nearest) / 60.0;
                }
                current = nearest;
            } else {
                break;
            }
        }
        
        // Return to starting point
        if (path.size() > 1) {
            Edge returnEdge = findDirectEdge(current, startUniversity);
            if (returnEdge != null) {
                totalCost += returnEdge.getCost();
                totalTime += returnEdge.getTravelTime();
            } else {
                double distance = calculateDistance(current, startUniversity);
                totalCost += distance * 150;
                totalTime += distance / 60.0;
            }
            path.add(startUniversity);
        }
        
        TSPResult result = new TSPResult(path, totalCost, totalTime);
        result.setVisitedOrder(new ArrayList<>(path));
        result.setDetails(String.format("Simple TSP using Nearest Neighbor\nTotal Cost: %.0f T\nTotal Time: %.1f hours", 
                                       totalCost, totalTime));
        
        return result;
    }
    
    // Show cost matrix (for debugging)
    public static String showCostMatrix(List<Node> nodes) {
        createMatrices(nodes);
        StringBuilder sb = new StringBuilder();
        sb.append("Cost Matrix:\n");
        
        // Header
        sb.append("     ");
        for (Node node : nodes) {
            sb.append(String.format("%-8s", node.getName().substring(0, Math.min(6, node.getName().length()))));
        }
        sb.append("\n");
        
        // Rows
        for (int i = 0; i < nodes.size(); i++) {
            sb.append(String.format("%-8s", nodes.get(i).getName().substring(0, Math.min(6, nodes.get(i).getName().length()))));
            for (int j = 0; j < nodes.size(); j++) {
                sb.append(String.format("%-8.0f", costMatrix[i][j]));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    public static TSPResult solveTSP(List<Node> universities, Node startUniversity, OptimizationType optimizationType) {
        // For now, we'll use the simple TSP implementation
        return solveTSPSimple(universities, startUniversity);
    }
} 